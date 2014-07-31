"""
 Copyright (c) 2014 Intercoding Project

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 Author: Marco Antonio Pinto O. <pinto.marco@live.com>
 URL: https://github.com/marcoapintoo
 License: LGPL
"""

from pinto import *
"""
Control structures"""

from java.lang import *
import java.lang
class Test04(java.lang.Object):
	"""
	 Auto generated constructor
	"""
	@multimethod()
	def __init__(self):
		pass
	
	"""
	 Auto generated constructor
	"""
	@classmethod
	@multimethod()
	def __class_init__(cls):
		pass
	
	@multimethod(limit = java.lang.Long)
	def testingIf1(self, limit):
		if true:
			System.out.println("Counter = " + i)
	
	@multimethod(limit = java.lang.Long)
	def testingIf2(self, limit):
		i = 0
		i += 1
		if i == 0:
			System.out.println("Counter is zero")
	
	@multimethod(limit = java.lang.Long)
	def testingIf3(self, limit):
		i = 0
		i += 1
		if i == 0:
			System.out.println("Counter is zero")
		else:
			System.out.println("Counter is not zero")
	
	@multimethod(limit = java.lang.Long)
	def testingFor1(self, limit):
		i = 0
		while i <= limit:
			System.out.println("Counter = " + i)
			i += 1
	
	@multimethod(limit = java.lang.Long)
	def testingFor2(self, limit):
		i = 0
		while i <= limit:
			System.out.println("Counter = " + i)
			i += 1
	
	@multimethod(limit = java.lang.Long)
	def testingWhile1(self, limit):
		i = 0
		while i <= limit:
			System.out.println("Counter = " + i)
			i += 1
	
	@multimethod(limit = java.lang.Long)
	def testingDoWhile1(self, limit):
		i = 0
		while i <= limit:
			System.out.println("Counter = " + i)
			i += 1

Test04.__class_init__()
