/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.net.jms.nativeimpl.endpoint.queue.sender;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.JMSUtils;
import org.ballerinalang.net.jms.nativeimpl.endpoint.common.SessionConnector;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;
import org.ballerinalang.util.exceptions.BallerinaException;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "jms",
        functionName = "initQueueSender",
        receiver = @Receiver(type = TypeKind.OBJECT,
                             structType = "QueueSender",
                             structPackage = "ballerina/jms"),
        args = {
                @Argument(name = "session",
                          type = TypeKind.OBJECT,
                          structType = "Session")
        }
)
public class InitQueueSender implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        Struct queueSenderBObject = BallerinaAdapter.getReceiverObject(context);
        Struct queueSenderConfig = queueSenderBObject.getStructField(Constants.QUEUE_SENDER_FIELD_CONFIG);
        String queueName = queueSenderConfig.getStringField(Constants.QUEUE_SENDER_FIELD_QUEUE_NAME);

        if (JMSUtils.isNullOrEmptyAfterTrim(queueName)) {
            throw new BallerinaException("Queue name cannot be null", context);
        }

        BStruct sessionBObject = (BStruct) context.getRefArgument(1);
        Session session = BallerinaAdapter.getNativeObject(sessionBObject,
                                                           Constants.JMS_SESSION,
                                                           Session.class,
                                                           context);
        try {
            Queue queue = null;
            if (!JMSUtils.isNullOrEmptyAfterTrim(queueName)) {
                queue = session.createQueue(queueName);
            }
            MessageProducer producer = session.createProducer(queue);
            Struct queueSenderConnectorBObject
                    = queueSenderBObject.getStructField(Constants.QUEUE_SENDER_FIELD_PRODUCER_ACTIONS);
            queueSenderConnectorBObject.addNativeData(Constants.JMS_PRODUCER_OBJECT, producer);
            queueSenderConnectorBObject.addNativeData(Constants.SESSION_CONNECTOR_OBJECT,
                                                      new SessionConnector(session));
        } catch (JMSException e) {
            BallerinaAdapter.throwBallerinaException("Error creating queue sender.", context, e);
        }
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
