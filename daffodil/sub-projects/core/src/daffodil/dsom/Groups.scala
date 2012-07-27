package daffodil.dsom

import scala.xml._
import scala.xml.parsing._
import daffodil.xml._
import daffodil.exceptions._
import daffodil.schema.annotation.props._
import daffodil.schema.annotation.props.gen._
import java.io.ByteArrayInputStream
import java.io.InputStream
import scala.collection.JavaConversions._
import daffodil.grammar._

/////////////////////////////////////////////////////////////////
// Groups System
/////////////////////////////////////////////////////////////////

// A term is content of a group
abstract class Term(xmlArg: Node, val parent: SchemaComponent, val position: Int)
  extends AnnotatedSchemaComponent(xmlArg)
  with LocalComponentMixin
  with DFDLStatementMixin
  with TermGrammarMixin
  with DelimitedRuntimeValuedPropertiesMixin
  with InitiatedTerminatedMixin{
  
  val enclosingComponent : Option[SchemaComponent] = Some(parent) // for global objects, the enclosing will be the thing referencing them.

  def isScalar = true // override in local elements
  
  lazy val allTerminatingMarkup: List[CompiledExpression] = {
    val tm = List(this.terminator) ++ this.allParentTerminatingMarkup
    tm.filter(x => x.isKnownNonEmpty)
  }

  lazy val allParentTerminatingMarkup: List[CompiledExpression] = {
    // Retrieves the terminating markup for all parent
    // objects
    //
    val pTM = parent match {
      case s: Sequence => List(s.separator, s.terminator) ++ s.allParentTerminatingMarkup
      case c: Choice => c.allParentTerminatingMarkup
      case d: SchemaDocument => List.empty
      case ct: LocalComplexTypeDef => ct.parent match {
        case local: LocalElementDecl => List(local.terminator) ++ local.allParentTerminatingMarkup
        case global: GlobalElementDecl => {
          global.elementRef match {
            case None => List(global.terminator)
            case Some(eRef) => eRef.allParentTerminatingMarkup
          }
        }
        case _ => Assert.impossibleCase()
      }
      // global type, we have to follow back to the element referencing this type
      case ct: GlobalComplexTypeDef => {
        // Since we are a term directly inside a global complex type def,
        // our nearest enclosing sequence is the one enclosing the element that
        // has this type. 
        //
        // However, that element might be local, or might be global and be referenced
        // from an element ref.
        //
        ct.element match {
          case local: LocalElementDecl => List(local.terminator) ++ local.allParentTerminatingMarkup
          case global: GlobalElementDecl => {
            global.elementRef match {
              case None => List(global.terminator)
              case Some(eRef) => eRef.allParentTerminatingMarkup
            }
          }
          case _ => Assert.impossibleCase()
        }
      }
      case gd: GlobalGroupDef => gd.groupRef.allParentTerminatingMarkup
      // We should only be asking for the enclosingSequence when there is one.
      case _ => Assert.invariantFailed("No parent terminating markup for : " + this)
    }
    val res = pTM.filter(x => x.isKnownNonEmpty)
    res
  }
  
  /**
   * nearestEnclosingSequence
   *
   * An attribute that looks upward to the surrounding
   * context of the schema, and not just lexically surrounding context. It needs to see
   * what declarations will physically surround the place. This is the dynamic scope,
   * not just the lexical scope. So, a named global type still has to be able to
   * ask what sequence is surrounding the element that references the global type.
   *
   * This is why we have to have the GlobalXYZDefFactory stuff. Because this kind of back
   * pointer (contextual sensitivity) prevents sharing.
   */
  lazy val nearestEnclosingSequence: Option[Sequence] = {
    // TODO: verify this is not just lexical scope containing. It's the scope of physical containment, so 
    // must also take into consideration references (element ref to element decl, element decl to type, type to group,
    // groupref to group)
    val res = parent match {
      case s: Sequence => Some(s)
      case c: Choice => c.nearestEnclosingSequence
      case d: SchemaDocument => None
      case ct: LocalComplexTypeDef => ct.parent match {
        case local: LocalElementDecl => local.nearestEnclosingSequence
        case global: GlobalElementDecl => {
          global.elementRef match {
            case None => None
            case Some(eRef) => eRef.nearestEnclosingSequence
          }
        }
        case _ => Assert.impossibleCase()
      }
      // global type, we have to follow back to the element referencing this type
      case ct: GlobalComplexTypeDef => {
        // Since we are a term directly inside a global complex type def,
        // our nearest enclosing sequence is the one enclosing the element that
        // has this type. 
        //
        // However, that element might be local, or might be global and be referenced
        // from an element ref.
        //
        ct.element match {
          case local: LocalElementDecl => local.nearestEnclosingSequence
          case global: GlobalElementDecl => {
            global.elementRef match {
              case None => None
              case Some(eRef) => eRef.nearestEnclosingSequence
            }
          }
          case _ => Assert.impossibleCase()
        }
      }
      case gd: GlobalGroupDef => gd.groupRef.nearestEnclosingSequence
      // We should only be asking for the enclosingSequence when there is one.
      case _ => Assert.invariantFailed("No enclosing sequence for : " + this)
    }
    res
  }
  
  lazy val positionInNearestEnclosingSequence : Int = {
    val res = 
      if (enclosingComponent == nearestEnclosingSequence) position
      else {
        enclosingComponent match {
          case Some(term : Term) => term.positionInNearestEnclosingSequence
          case Some(ct : ComplexTypeBase) => {
            val ctElem = ct.element
            val ctPos = ctElem.positionInNearestEnclosingSequence
            ctPos
          }
          case _ => Assert.invariantFailed("unable to compute position in nearest enclosing sequence")
        }
      }
    res
  }

  lazy val terminatingMarkup: List[CompiledExpression] = {
    if (hasTerminator) List(terminator)
    else nearestEnclosingSequence match {
      case None => Nil
      case Some(sq) => {
        val sep = {
          if (sq.hasInfixSep || sq.hasPostfixSep) List(sq.separator)
          else Nil
        }
        if (!hasLaterRequiredSiblings) {
          val entm = sq.terminatingMarkup
          val res = sep ++ entm
          res
        } else {
          sep
        }
      }
    }
  }
  
  lazy val prettyTerminatingMarkup =
    terminatingMarkup.map { _.prettyExpr }.map { "'" + _ + "'" }.mkString(" ")

  lazy val isDirectChildOfSequence = parent.isInstanceOf[Sequence]

  import daffodil.util.ListUtils

  lazy val hasLaterRequiredSiblings: Boolean = hasRequiredSiblings(ListUtils.tailAfter _)
  lazy val hasPriorRequiredSiblings: Boolean = hasRequiredSiblings(ListUtils.preceding _)

  def hasRequiredSiblings(splitter: ListUtils.SubListFinder[Term]) = {
    val res = nearestEnclosingSequence.map { es =>
      {
        val allSiblings = es.groupMembers
        val sibs = splitter(allSiblings, this)
        val hasAtLeastOne = sibs.find { term => term.hasStaticallyRequiredInstances }
        hasAtLeastOne != None
      }
    }.getOrElse(false)
    res
  }

  def hasStaticallyRequiredInstances: Boolean

}

abstract class GroupBase(xmlArg: Node, parent: SchemaComponent, position: Int)
  extends Term(xmlArg, parent, position) {

  lazy val detailName = ""
  def group: ModelGroup

  lazy val localAndFormatRefProperties = { this.formatAnnotation.getFormatPropertiesNonDefault() }

  lazy val localProperties = {
    // Properties that exist directly on the object in
    // short or long form
    this.formatAnnotation.combinedLocalProperties
  }

  lazy val formatRefProperties = {
    // Properties coming from named format ref from
    // the format annotation
    this.formatAnnotation.formatRefProperties
  }

  lazy val immediateGroup: Option[GroupBase] = {
    
    val res: Option[GroupBase] = this.group match {
      case (s: Sequence) => Some(s)
      case (c: Choice) => Some(c)
      case (g: GroupRef) => Some(g)
      case _ => None
    }
    
    res
  }

}

/**
 * Base class for all model groups, which are term containers.
 */
abstract class ModelGroup(xmlArg: Node, parent: SchemaComponent, position: Int)
  extends GroupBase(xmlArg, parent, position)
  with ModelGroupGrammarMixin {

  lazy val prettyName = xmlArg.label
  
  val xmlChildren: Seq[Node]

  private val goodXmlChildren = xmlChildren.flatMap { removeNonInteresting(_) }
  private val positions = List.range(1, goodXmlChildren.length + 1) // range is exclusive on 2nd arg. So +1.
  private val pairs = goodXmlChildren zip positions
  private lazy val children = pairs.flatMap {
    case (n, i) =>
       termFactory(n, this, i) 
  }

  def group = this

  lazy val groupMembers_ = LV{
    children
  }
  lazy val groupMembers = groupMembers_.value
  
  lazy val diagnosticChildren = annotationObjs ++ children

  /**
   * Factory for Terms
   *
   * Because of the context where this is used, this returns a list. Nil for non-terms, non-Nil for
   * an actual term. There should be only one non-Nil.
   *
   * This could be static code in an object. It doesn't reference any of the state of the ModelGroup,
   * it's here so that type-specific overrides are possible in Sequence or Choice
   */
  def termFactory(child: Node, parent: ModelGroup, position: Int) = {
    val childList: List[Term] = child match {
      case <element>{ _* }</element> => {
        val refProp = (child \ "@ref").text
        if (refProp == "") List(new LocalElementDecl(child, parent, position))
        else List(new ElementRef(child, parent, position))
      }
      case <annotation>{ _* }</annotation> => Nil
      case textNode: Text => Nil
      case _ => GroupFactory(child, parent, position)
    }
    childList
  }

  /**
   * XML is full of uninteresting text nodes. We just want the element children, not all children.
   */
  def removeNonInteresting(child: Node) = {
    val childList: List[Node] = child match {
      case _: Text => Nil
      case <annotation>{ _* }</annotation> => Nil
      case _ => List(child)
    }
    childList
  }

  lazy val myGroupReferenceProps: Map[String, String] = {
    val noProps = Map.empty[String, String]
    parent match {
      case ggd: GlobalGroupDef => ggd.groupRef.localAndFormatRefProperties
      case mg: ModelGroup => noProps
      case ct: ComplexTypeBase => noProps
      case _ => Assert.invariantFailed("parent of group is not one of the allowed parent types.")
    }
  }

  lazy val overlappingProps: Set[String] = {
    val parentProps = myGroupReferenceProps.keySet
    val localProps = this.localAndFormatRefProperties.keySet
    val theIntersect = parentProps.intersect(localProps)
    theIntersect
  }

  lazy val combinedGroupRefAndGlobalGroupDefProperties: Map[String, String] = {
    schemaDefinition(overlappingProps.size == 0,
      "Overlap detected between the properties in the model group of a global group definition (%s) and its group reference.", 
      this.detailName)

    val props = myGroupReferenceProps ++ this.localAndFormatRefProperties
    props
  }
  
  override lazy val allNonDefaultProperties: Map[String, String] = {
    val theLocalUnion = this.combinedGroupRefAndGlobalGroupDefProperties
    theLocalUnion
  }

}

/**
 * A factory for model groups.
 */
object GroupFactory {

  /**
   * Because of the contexts where this is used, we return a list. That lets users
   * flatmap it to get a collection of model groups. Nil for non-model groups, non-Nil for the model group
   * object. There should be only one non-Nil.
   */
  def apply(child: Node, parent: SchemaComponent, position: Int) = {
    val childList: List[GroupBase] = child match {
      case <sequence>{ _* }</sequence> => List(new Sequence(child, parent, position))
      case <choice>{ _* }</choice> => List(new Choice(child, parent, position))
      case <group>{ _* }</group> => {
        parent match {
          case ct: ComplexTypeBase => List(new GroupRef(child, ct, 1))
          case mg: ModelGroup => List(new GroupRef(child, mg, position))
        }
      }
      case <annotation>{ _* }</annotation> => Nil
      case textNode: Text => Nil
      case _ => Assert.impossibleCase()
    }
    childList
  }

}
/**
 * Choices are a bit complicated.
 *
 * They can have initiators and terminators. This is most easily thought of as wrapping each choice
 * inside a sequence having only the choice inside it, and moving the initiator and terminator specification to that
 * sequence.
 *
 * That sequence is then the term replacing the choice wherever the choice is.
 *
 * Choices can have children which are scalar elements. In this case, that scalar element behaves as if it were
 * a child of the enclosing sequence (which could be the one we just injected above the choice.
 *
 * Choices can have children which are recurring elements. In this case, the behavior is as if the recurring child was
 * placed inside a sequence which has no initiator nor terminator, but repeats the separator specification from
 * the sequence context that encloses the choice.
 *
 * All that, and the complexities of separator suppression too.
 *
 * There's also issues like this:
 *
 * <choice>
 *    <element .../>
 *    <sequence/>
 * </choice>
 *
 * in the above, one alternative is an empty sequence. So this choice may produce an element which takes up
 * a child position, and potentially requires separation, or it may produce nothing at all.
 *
 * So, to keep things managable, we're going to start with some restrictions
 *
 * 1) all children of a choice must be scalar elements
 * 2) no initiators nor terminators on choices. (Just wrap in a sequence if you care.)
 *
 */

class Choice(xmlArg: Node, parent: SchemaComponent, position: Int)
  extends ModelGroup(xmlArg, parent, position)
  with ChoiceGrammarMixin {

  def annotationFactory(node: Node): DFDLAnnotation = {
    node match {
      case <dfdl:choice>{ contents @ _* }</dfdl:choice> => new DFDLChoice(node, this)
      case _ => annotationFactoryForDFDLStatement(node, this)
    }
  }

  def emptyFormatFactory = new DFDLChoice(newDFDLAnnotationXML("choice"), this)
  def isMyAnnotation(a: DFDLAnnotation) = a.isInstanceOf[DFDLChoice]

  lazy val <choice>{ xmlChildren @ _* }</choice> = xml

  lazy val hasStaticallyRequiredInstances = {
    // true if all arms of the choice have statically required instances.
    groupMembers.forall { _.hasStaticallyRequiredInstances }
  }

  /**
   * We override termFactory because we're only going to support a subset of the full
   * generality of what could go inside a choice.
   *
   * TODO: someday lift this restriction.
   */
  override def termFactory(child: Node, parent: ModelGroup, position: Int) = {
    val childList: List[Term] = child match {
      case <element>{ _* }</element> => {
        val refProp = (child \ "@ref").text
        val elt =
          if (refProp == "") new LocalElementDecl(child, parent, position)
          else new ElementRef(child, parent, position)
        subset(elt.isScalar, "Choices may only have scalar element children (minOccurs = maxOccurs = 1).")
        List(elt)
      }
      case <annotation>{ _* }</annotation> => Nil
      case textNode: Text => Nil
      case _ => subsetError("Non-element child type. Choices may only have scalar element children (minOccurs = maxOccurs = 1).")
    }
    childList
  }
}

class Sequence(xmlArg: Node, parent: SchemaComponent, position: Int)
  extends ModelGroup(xmlArg, parent, position)
  with Sequence_AnnotationMixin
  with SequenceRuntimeValuedPropertiesMixin
  with SequenceGrammarMixin
  with SeparatorSuppressionPolicyMixin {

  def annotationFactory(node: Node): DFDLAnnotation = {
    node match {
      case <dfdl:sequence>{ contents @ _* }</dfdl:sequence> => new DFDLSequence(node, this)
      case _ => annotationFactoryForDFDLStatement(node, this)
    }
  }

  def emptyFormatFactory = new DFDLSequence(newDFDLAnnotationXML("sequence"), this)
  def isMyAnnotation(a: DFDLAnnotation) = a.isInstanceOf[DFDLSequence]

  lazy val <sequence>{ xmlChildren @ _* }</sequence> = xml

  lazy val hasStaticallyRequiredInstances = {
    // true if any child of the sequence has statically required instances.
    groupMembers.exists { _.hasStaticallyRequiredInstances }
  }

}

class GroupRef(xmlArg: Node, parent: SchemaComponent, position: Int)
  extends GroupBase(xmlArg, parent, position)
  with GroupRefGrammarMixin {
  
  lazy val prettyName = "groupRef"
    
  def annotationFactory(node: Node): DFDLAnnotation = {
    node match {
      case <dfdl:group>{ contents @ _* }</dfdl:group> => new DFDLGroup(node, this)
      case _ => annotationFactoryForDFDLStatement(node, this)
    }
  }

  def emptyFormatFactory = new DFDLGroup(newDFDLAnnotationXML("group"), this)
  def isMyAnnotation(a: DFDLAnnotation) = a.isInstanceOf[DFDLGroup]

  def hasStaticallyRequiredInstances = Assert.notYetImplemented()

  // TODO: Consolidate techniques with HasRef trait used by ElementRef
  lazy val refName = {
    val str = (xml \ "@ref").text
    if (str == "") None else Some(str)
  }

  lazy val refQName = {
    refName match {
      case Some(rname) => Some(XMLUtils.QName(xml, rname, schemaDocument))
      case None => None
    }
  }

  lazy val group = groupDef.modelGroup 
    
  lazy val groupDef : GlobalGroupDef = LV {
    val res = refQName match {
      // TODO See comment above about consolidating techniques.
      case None => schemaDefinitionError("No group definition found for " + refName + ".")
      case Some((ns, localpart)) => {
        val ss = schema.schemaSet
        val ggdf = ss.getGlobalGroupDef(ns, localpart)
        val res = ggdf match {
          case Some(ggdFactory) => ggdFactory.forGroupRef(this, position)
          case None => schemaDefinitionError("No group definition found for " + refName + ".")
          // FIXME: do we need to do these checks, or has schema validation checked this for us?
          // FIXME: if we do have to check, then the usual problems: don't stop on first error, and need location of error in diagnostic.
        }
        res
      }
    }
    res
  }
  
  lazy val diagnosticChildren : Seq[DiagnosticsProviding] = annotationObjs :+ groupDef

}

class GlobalGroupDefFactory(val xml: Node, schemaDocument: SchemaDocument)
extends NamedMixin
{
  //  def forComplexType(ct : ComplexTypeBase) = {
  //    new GlobalGroupDef(xmlArg, schemaDocument, ct, 1)
  //  }

  def forGroupRef(gref: GroupRef, position: Int) = {
    new GlobalGroupDef(xml, schemaDocument, gref, position)
  }
}

class GlobalGroupDef(val xmlArg: Node, val schemaDocument: SchemaDocument, val groupRef: GroupRef, position: Int)
  extends SchemaComponent(xmlArg) with GlobalComponentMixin {
  //
  // Note: Dealing with XML can be fragile. It's easy to forget some of these children
  // might be annotations and Text nodes. Even if you trim the text nodes out, there are
  // places where annotations can be.
  //
  lazy val <group>{ xmlChildren @ _* }</group> = xml
  //
  // So we have to flatMap, so that we can tolerate annotation objects (like documentation objects).
  // and our ModelGroup factory has to return Nil for annotations and Text nodes.
  //
  lazy val Seq(modelGroup: ModelGroup) = xmlChildren.flatMap { GroupFactory(_, this, position) } 
  
  lazy val diagnosticChildren = List(modelGroup)

}

