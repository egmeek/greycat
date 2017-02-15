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
package greycat.ml.neuralnet.layer;

import greycat.ml.neuralnet.process.ExMatrix;
import greycat.ml.neuralnet.process.ProcessGraph;
import greycat.struct.ENode;

import java.util.Random;

/**
 * Created by assaad on 14/02/2017.
 */
public class LSTM implements Layer {
    public LSTM(ENode hostnode) {

    }

    @Override
    public void fillWithRandom(Random random, double min, double max) {

    }

    @Override
    public void fillWithRandomStd(Random random, double std) {

    }

    @Override
    public ExMatrix forward(ExMatrix input, ProcessGraph g) {
        return null;
    }

    @Override
    public void resetState() {

    }

    @Override
    public ExMatrix[] getModelParameters() {
        return new ExMatrix[0];
    }
}