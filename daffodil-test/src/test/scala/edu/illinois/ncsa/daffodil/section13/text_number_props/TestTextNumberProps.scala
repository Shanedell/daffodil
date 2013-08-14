package edu.illinois.ncsa.daffodil.section13.text_number_props

/* Copyright (c) 2013 Tresys Technology, LLC. All rights reserved.
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
import org.junit.Test
import scala.xml._
import edu.illinois.ncsa.daffodil.xml.XMLUtils
import edu.illinois.ncsa.daffodil.xml.XMLUtils._
import edu.illinois.ncsa.daffodil.compiler.Compiler
import edu.illinois.ncsa.daffodil.util._
import edu.illinois.ncsa.daffodil.tdml.DFDLTestSuite
import java.io.File

class TestTextNumberProps {
  val testDir = "/edu/illinois/ncsa/daffodil/section13/text_number_props/"
  val aa = testDir + "TextNumberProps.tdml"
  lazy val runner = new DFDLTestSuite(Misc.getRequiredResource(aa))

  @Test def test_textNumberPattern_baseConflict() { runner.runOneTest("textNumberPattern_baseConflict") }
  @Test def test_textNumberPattern_positiveMandatory() { runner.runOneTest("textNumberPattern_positiveMandatory") }
  @Test def test_textNumberPattern_negativeOptional() { runner.runOneTest("textNumberPattern_negativeOptional") }

//  @Test def test_textNumberPattern_negativeIgnored01() { runner.runOneTest("textNumberPattern_negativeIgnored01") }
//  @Test def test_textNumberPattern_negativeIgnored02() { runner.runOneTest("textNumberPattern_negativeIgnored02") }
//  @Test def test_textNumberPattern_negativeIgnored03() { runner.runOneTest("textNumberPattern_negativeIgnored03") }
  @Test def test_textNumberPattern_negativeIgnored04() { runner.runOneTest("textNumberPattern_negativeIgnored04") }

  @Test def test_textNumberPattern_exponent01() { runner.runOneTest("textNumberPattern_exponent01") }
  @Test def test_textNumberPattern_specialChar01() { runner.runOneTest("textNumberPattern_specialChar01") }
  @Test def test_textNumberPattern_specialChar02() { runner.runOneTest("textNumberPattern_specialChar02") }
  @Test def test_textNumberPattern_specialChar03() { runner.runOneTest("textNumberPattern_specialChar03") }
  @Test def test_textNumberPattern_specialChar04() { runner.runOneTest("textNumberPattern_specialChar04") }

// DFDL-845
//  @Test def test_textNumberCheckPolicy_lax01() { runner.runOneTest("textNumberCheckPolicy_lax01") }
//  @Test def test_textNumberCheckPolicy_lax05() { runner.runOneTest("textNumberCheckPolicy_lax05") }

  @Test def test_textNumberCheckPolicy_lax02() { runner.runOneTest("textNumberCheckPolicy_lax02") }
  @Test def test_textNumberCheckPolicy_lax03() { runner.runOneTest("textNumberCheckPolicy_lax03") }
// leading/trailing whitespace and leading zeros should be ignored.
//  @Test def test_textNumberCheckPolicy_lax04() { runner.runOneTest("textNumberCheckPolicy_lax04") }

  @Test def test_textNumberCheckPolicy_strict01() { runner.runOneTest("textNumberCheckPolicy_strict01") }
  @Test def test_textNumberCheckPolicy_strict02() { runner.runOneTest("textNumberCheckPolicy_strict02") }
//  @Test def test_textNumberCheckPolicy_strict03() { runner.runOneTest("textNumberCheckPolicy_strict03") }
  @Test def test_textNumberCheckPolicy_strict04() { runner.runOneTest("textNumberCheckPolicy_strict04") }
  
//  @Test def test_textStandardDecimalSeparator01() { runner.runOneTest("textStandardDecimalSeparator01") }
  @Test def test_textStandardDecimalSeparator02() { runner.runOneTest("textStandardDecimalSeparator02") }
//  @Test def test_textStandardDecimalSeparator03() { runner.runOneTest("textStandardDecimalSeparator03") }
  @Test def test_textStandardDecimalSeparator04() { runner.runOneTest("textStandardDecimalSeparator04") }
  @Test def test_textStandardDecimalSeparator05() { runner.runOneTest("textStandardDecimalSeparator05") }
  @Test def test_textStandardDecimalSeparator06() { runner.runOneTest("textStandardDecimalSeparator06") }
  @Test def test_textStandardDecimalSeparator07() { runner.runOneTest("textStandardDecimalSeparator07") }
  @Test def test_textStandardDecimalSeparator08() { runner.runOneTest("textStandardDecimalSeparator08") }
  @Test def test_textStandardDecimalSeparator09() { runner.runOneTest("textStandardDecimalSeparator09") }
  
  @Test def test_textStandardGroupingSeparator01() { runner.runOneTest("textStandardGroupingSeparator01") }
  @Test def test_textStandardGroupingSeparator02() { runner.runOneTest("textStandardGroupingSeparator02") }
// DFDL-843
//  @Test def test_textStandardGroupingSeparator03() { runner.runOneTest("textStandardGroupingSeparator03") }
  @Test def test_textStandardGroupingSeparator04() { runner.runOneTest("textStandardGroupingSeparator04") }
  @Test def test_textStandardGroupingSeparator05() { runner.runOneTest("textStandardGroupingSeparator05") }
  
  @Test def test_dynamic() { runner.runOneTest("dynamic") }
  @Test def test_dynamic_neg1() { runner.runOneTest("dynamicNeg1") }
  @Test def test_dynamic_neg2() { runner.runOneTest("dynamicNeg2") }

}
