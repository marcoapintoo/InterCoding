/*
 *  Copyleft (c) 2014 InterCoding Project
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *  Author: Marco Antonio Pinto O. <pinto.marco@live.com>
 *  URL: https://github.com/marcoapintoo
 *  License: LGPL
 */
package org.pinto.intercoding.intermediate

import groovy.transform.CompileStatic


@CompileStatic
class ExpressionList extends OwnerArrayList<ExpressionModel, BaseModel> {
    ExpressionList(BaseModel owner, List<? extends ExpressionModel> baselist) {
        super(owner, adaptCollections(baselist))
        //super(owner, baselist)
    }

    ExpressionList(BaseModel owner) {
        super(owner)
    }

    @Override
    void setParenthood(ExpressionModel object, BaseModel parent) {
        //object.statementOwnerList=this
        object.accessor = new CollectionPropertyModelAccesor<StatementModel>(provider: this, containedObject: object, owner: owner)
        if (parent instanceof StatementModel) {
            //object.statementOwner = parent
        } else if (parent instanceof TypeElementModel) {
            //object.elementOwner = parent
        } else {
            throw new Exception("I can't handle ${parent.class} with expressions!")
        }
    }

    @Override
    ExpressionModel createDefault() {
        return LiteralModel.None.copy()
    }

    boolean add(BlockModel block) {
        return super.addAll(block.statements)
    }

    private static ArrayList<? extends ExpressionModel> adaptCollections(Collection<? extends StatementModel> c) {
        ArrayList<? extends ExpressionModel> newList = new ArrayList<? extends ExpressionModel>()
        for (item in c) {
            if (item instanceof BlockModel) {
                newList.addAll((item as BlockModel).statements.collect { (ExpressionModel) it })
            } else {
                newList.add((ExpressionModel) item)
            }
        }
        return newList
    }

    @Override
    boolean addAll(int index, Collection<? extends ExpressionModel> c) {
        return super.addAll(index, adaptCollections(c))
    }
}

@CompileStatic
class StatementList extends OwnerArrayList<StatementModel, BaseModel> {
    StatementList(BaseModel owner, List<? extends StatementModel> baselist) {
        super(owner, baselist)
    }

    StatementList(BaseModel owner) {
        super(owner)
    }

    @Override
    void setParenthood(StatementModel object, BaseModel parent) {
        //object.statementOwnerList=this
        object.accessor = new CollectionPropertyModelAccesor<StatementModel>(provider: this, containedObject: object, owner: owner)
        if (parent instanceof StatementModel) {
            //object.statementOwner = parent
        } else if (parent instanceof TypeElementModel) {
            //object.elementOwner = parent
        } else {
            throw new Exception("I don't know how to handle ${parent.class} with statements")
        }
    }

    @Override
    StatementModel createDefault() {
        return new EmptyModel()
    }
}

class ElementList extends OwnerArrayList<TypeElementModel, BaseModel> {
    ElementList(BaseModel owner, List<TypeElementModel> baselist) {
        super(owner, baselist)
    }

    ElementList(BaseModel owner) {
        super(owner)
    }

    @Override
    TypeElementModel createDefault() {
        return null
    }

    @Override
    void setParenthood(TypeElementModel object, BaseModel parent) {
        object.typeOwner = parent as TypeModel
    }
}


class TypeList extends OwnerArrayList<TypeModel, BaseModel> {
    TypeList(BaseModel owner, List<? extends TypeModel> baselist) {
        super(owner, baselist)
    }

    TypeList(BaseModel owner) {
        super(owner)
    }

    @Override
    TypeModel createDefault() {
        return null
    }

    @Override
    void setParenthood(TypeModel object, BaseModel parent) {
        if (parent instanceof TypeModel) {
            object.typeOwner = parent
        } else if (parent instanceof NamespaceModel) {
            object.namespaceOwner = parent
        } else {
            log.error "I don't know how to handle a " + parent.toString() + " as parent of the type " + object.toString()
        }
    }

}
