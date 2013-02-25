package edu.illinois.ncsa.daffodil.section06.namespaces

/* Copyright (c) 2012-2013 Tresys Technology, LLC. All rights reserved.
 *
 * Developed by: Tresys Technology, LLC
 *               http://www.tresys.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal with
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimers.
 * 
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimers in the
 *     documentation and/or other materials provided with the distribution.
 * 
 *  3. Neither the names of Tresys Technology, nor the names of its contributors
 *     may be used to endorse or promote products derived from this Software
 *     without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE
 * SOFTWARE.
 */


import junit.framework.Assert._
import org.scalatest.junit.JUnitSuite
import org.junit.Test
import scala.xml._
import edu.illinois.ncsa.daffodil.xml.XMLUtils
import edu.illinois.ncsa.daffodil.xml.XMLUtils._
import edu.illinois.ncsa.daffodil.compiler.Compiler
import edu.illinois.ncsa.daffodil.util._
import edu.illinois.ncsa.daffodil.tdml.DFDLTestSuite
import java.io.File

class TestNamespaces extends JUnitSuite {
  val testDir = "/edu/illinois/ncsa/daffodil/section06/namespaces/"
  val aa = testDir + "namespaces.tdml"
  lazy val runner = new DFDLTestSuite(Misc.getRequiredResource(aa))

  @Test def test_Lesson2_no_namespace() { runner.runOneTest("Lesson2_no_namespace") }
  @Test def test_Lesson2_include_schema() { runner.runOneTest("Lesson2_include_schema") }
  @Test def test_Lesson2_import_schema() { runner.runOneTest("Lesson2_import_schema") }

//  @Test def test_multifile_cyclical() { runner.runOneTest("multifile_cyclical") }
  @Test def test_multifile_choice_01() { runner.runOneTest("multifile_choice_01") }
  @Test def test_multifile_choice_02() { runner.runOneTest("multifile_choice_02") }
//  @Test def test_multifile_choice_02b() { runner.runOneTest("multifile_choice_02b") }
  @Test def test_multifile_choice_03() { runner.runOneTest("multifile_choice_03") }
  
  @Test def test_multifile_facets_01() { runner.runOneTest("multifile_facets_01") }
  @Test def test_multifile_facets_02() { runner.runOneTest("multifile_facets_02") }
  @Test def test_multifile_facets_03() { runner.runOneTest("multifile_facets_03") }
  @Test def test_multifile_facets_04() { runner.runOneTest("multifile_facets_04") }

  @Test def test_double_nesting_01() { runner.runOneTest("double_nesting_01") }
  
  @Test def test_scope_01() { runner.runOneTest("scope_01") }
  @Test def test_scope_02() { runner.runOneTest("scope_02") }

  @Test def test_long_chain_01() { runner.runOneTest("long_chain_01") }
  @Test def test_long_chain_02() { runner.runOneTest("long_chain_02") }
  @Test def test_long_chain_03() { runner.runOneTest("long_chain_03") }
  @Test def test_long_chain_04() { runner.runOneTest("long_chain_04") }
  @Test def test_long_chain_05() { runner.runOneTest("long_chain_05") }
  @Test def test_long_chain_06() { runner.runOneTest("long_chain_06") }
  @Test def test_long_chain_06b() { runner.runOneTest("long_chain_06b") }
  @Test def test_long_chain_07() { runner.runOneTest("long_chain_07") }
  
//  @Test def test_namespace_conflict_01() { runner.runOneTest("namespace_conflict_01") }

  @Test def test_no_namespace_01() { runner.runOneTest("no_namespace_01") }
//  @Test def test_no_namespace_02() { runner.runOneTest("no_namespace_02") }
  @Test def test_no_namespace_03() { runner.runOneTest("no_namespace_03") }
  @Test def test_no_namespace_04() { runner.runOneTest("no_namespace_04") }

}
