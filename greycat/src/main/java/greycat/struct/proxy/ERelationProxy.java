/**
 * Copyright 2017 The GreyCat Authors.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greycat.struct.proxy;

import greycat.Container;
import greycat.struct.EGraph;
import greycat.struct.ENode;
import greycat.struct.ERelation;

public class ERelationProxy implements ERelation {

    private final int _relationIndex;
    private Container _target;
    private ERelation _relation;

    public ERelationProxy(final int _relationIndex, final Container _target, final ERelation _relation) {
        this._relationIndex = _relationIndex;
        this._target = _target;
        this._relation = _relation;
    }

    private void check() {
        if (_target != null) {
            _relation = (ERelation) _target.rephase().getRawAt(_relationIndex);
            _target = null;
        }
    }

    @Override
    public final ENode[] nodes() {
        return _relation.nodes();
    }

    @Override
    public final int size() {
        return _relation.size();
    }

    @Override
    public final ENode node(final int index) {
        if (_target != null) {
            EGraph eg = ((ENode) _target).egraph();
            if (eg instanceof EGraphProxy) {
                return new ENodeProxy((EGraphProxy) eg, _relation.node(index), index);
            } else {
                return _relation.node(index);
            }
        } else {
            return _relation.node(index);
        }
    }

    @Override
    public final ERelation add(final ENode eNode) {
        check();
        return _relation.add(eNode);
    }

    @Override
    public final ERelation addAll(final ENode[] eNodes) {
        check();
        return _relation.addAll(eNodes);
    }

    @Override
    public final ERelation clear() {
        check();
        return _relation.clear();
    }

    @Override
    public final String toString() {
        return _relation.toString();
    }

}
