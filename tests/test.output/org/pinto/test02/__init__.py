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
class Test02(java.lang.Object):
	"""
	 Auto generated constructor
	"""
	@classmethod
	@multimethod()
	def __class_init__(cls):
		Test02.staticValue = 0
	
	staticValue = None
	
	name = None
	
	privateValue = None
	
	@multimethod()
	def __init__(self):
		Test02.staticValue += 1
		self.privateValue = Test02.staticValue
	
	@multimethod()
	def value(self):
		return self.privateValue
	
	@multimethod()
	def getName(self):
		return self.name
	
	@multimethod(name = String)
	def setName(self, name):
		if name == None:
			name = ""
		
		self.name = name

Test02.__class_init__()
