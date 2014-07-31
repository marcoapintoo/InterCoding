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

from java.lang import *
import java.lang
class _Helper_EnumPriceType_1(EnumPriceType):
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
	
	@multimethod(input = java.lang.Float)
	def getPrice(self, input):
		return input * 1.2
	
	@multimethod(input = java.lang.Float)
	def getFormattedPrice(self, input):
		return input * 1.2 + " €"

_Helper_EnumPriceType_1.__class_init__()


from java.lang import *
import java.lang
class _Helper_EnumPriceType_2(EnumPriceType):
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
	
	@multimethod(input = java.lang.Float)
	def getPrice(self, input):
		return input

_Helper_EnumPriceType_2.__class_init__()

"""
A little more complex POJO class"""

from java.lang import *
import java.lang
class Test08(java.lang.Object):
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
	def getValue(self):
		return self.value
	
	@multimethod(value = EnumPriceType)
	def setValue(self, value):
		self.value = value
	
	@multimethod()
	def getCurrentPrice(self):
		self.value.getPrice()

Test08.__class_init__()

# From StackOverFlow

from java.lang import *
import java.lang
class EnumPriceType(java.lang.Object):
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
		EnumPriceType.WITH_TAXES = _Helper_EnumPriceType_1()
		EnumPriceType.WITHOUT_TAXES = _Helper_EnumPriceType_2()
	
	@multimethod(input = java.lang.Float)
	def getPrice(self, input):
		pass
	
	@classmethod
	@multimethod(args = list)
	def main(cls, args):
		EnumPriceType.WITH_TAXES.getFormattedPrice(33.0)
	
	WITH_TAXES = None
	
	WITHOUT_TAXES = None

EnumPriceType.__class_init__()


from java.lang import *
import java.lang
class _Helper_EnumPriceType_1(EnumPriceType):
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
	
	@multimethod(input = java.lang.Float)
	def getPrice(self, input):
		return input * 1.2
	
	@multimethod(input = java.lang.Float)
	def getFormattedPrice(self, input):
		return input * 1.2 + " €"

_Helper_EnumPriceType_1.__class_init__()


from java.lang import *
import java.lang
class _Helper_EnumPriceType_2(EnumPriceType):
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
	
	@multimethod(input = java.lang.Float)
	def getPrice(self, input):
		return input

_Helper_EnumPriceType_2.__class_init__()
