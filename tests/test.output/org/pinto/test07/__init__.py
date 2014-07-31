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
A little more complex POJO class"""

from java.lang import *
import java.lang
class Test07(java.lang.Object):
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
	
	value = None
	
	@multimethod()
	def toC(self):
		self.value = TestEnum.OptionC
	
	@multimethod(value = TestEnum)
	def to(self, value):
		self.value = value

Test07.__class_init__()


from java.lang import *
import java.lang
class TestEnum(java.lang.Object):
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
		TestEnum.OptionA = TestEnum()
		TestEnum.OptionB = TestEnum()
		TestEnum.OptionC = TestEnum()
		TestEnum.OptionD = TestEnum()
	
	OptionA = None
	
	OptionB = None
	
	OptionC = None
	
	OptionD = None

TestEnum.__class_init__()
