package edu.illinois.ncsa.daffodil.processors.dfa

import edu.illinois.ncsa.daffodil.processors.DFDLCharReader
import edu.illinois.ncsa.daffodil.util.Maybe
import edu.illinois.ncsa.daffodil.util.Maybe._
import edu.illinois.ncsa.daffodil.dsom.RuntimeEncodingMixin
import edu.illinois.ncsa.daffodil.processors.EncodingInfo
import edu.illinois.ncsa.daffodil.processors.RuntimeData

class TextPaddingParser(val padChar: Char,
  override val context: RuntimeData,
  override val encodingInfo: EncodingInfo)
  extends Parser with RuntimeEncodingMixin {

  val paddingDFA = CreatePaddingDFA(padChar)

  def parse(input: DFDLCharReader): Maybe[ParseResult] = {

    val paddingReg: Registers = new Registers

    paddingReg.reset(input, 0)

    val dfaStatus = paddingDFA.run(0, paddingReg, 0) // Will always succeed.

    val paddingValue = One(paddingReg.resultString.toString)
    val totalNumCharsRead = paddingReg.numCharsReadUntilDelim
    val numBits: Int = knownEncodingStringBitLength(paddingReg.charsReadUntilDelim.toString)
    val nextReader: DFDLCharReader = input.drop(totalNumCharsRead).asInstanceOf[DFDLCharReader]
    One(new ParseResult(paddingValue, Nope, "", totalNumCharsRead, numBits, nextReader))
  }

}