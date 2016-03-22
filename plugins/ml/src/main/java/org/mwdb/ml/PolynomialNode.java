package org.mwdb.ml;

import org.mwdb.KNode;
import org.mwdb.KPolynomialNode;
import org.mwdb.KType;

public class PolynomialNode extends AbstractMLNode implements KPolynomialNode {

    private static final String VALUE_KEY = "val";

    public PolynomialNode(KNode p_rootNode) {
        super(p_rootNode);
    }

    @Override
    public void attSet(String attributeName, byte attributeType, Object attributeValue) {
        if (attributeName.equals(VALUE_KEY)) {
            learn((double) attributeValue);
        } else {
            rootNode().attSet(attributeName, attributeType, attributeValue);
        }
    }

    @Override
    public byte attType(String attributeName) {
        if (attributeName.equals(VALUE_KEY)) {
            return KType.DOUBLE;
        } else {
            return rootNode().attType(attributeName);
        }
    }

    @Override
    public Object att(String attributeName) {
        if (attributeName.equals(VALUE_KEY)) {
            return infer();
        } else {
            return rootNode().att(attributeName);
        }
    }

    @Override
    public void learn(double value) {
        //TODO
    }

    @Override
    public double infer() {
        //TODO
        return 0;
    }
}