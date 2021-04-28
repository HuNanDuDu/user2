package com.example.user1.mq;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JuseMqSender implements ApplicationContextAware {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Map<String, JuseMqHandler> receiverMapping = new HashMap();



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> receivers = applicationContext.getBeansWithAnnotation(JuseMqReceiver.class);
        if (!CollectionUtils.isEmpty(receivers)) {
            for (Object r : receivers.values()) {
                JuseMqReceiver receiver = r.getClass().getAnnotation(JuseMqReceiver.class);
                String prefix = receiver.value();
                for (Method method : r.getClass().getDeclaredMethods()) {
                    JuseMqReceiver methodReceiver = AnnotationUtils.getAnnotation(method, JuseMqReceiver.class);
                    if (null != methodReceiver) {
                        String key = (StringUtils.isEmpty(prefix) ? "" : prefix + ".") + methodReceiver.value();
                        receiverMapping.put(key, new JuseMqHandler(r, method));
                    }
                }
            }
        }
    }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "senduser",
                    durable = "true"),
            exchange = @Exchange(name = "test",
                    type = ExchangeTypes.TOPIC,
                    ignoreDeclarationExceptions = "true"),
            key = "user.#"
    )
    )
    @RabbitHandler
    public void handleMessage(Message message, @Headers Map<String,Object> headers, Channel channel) throws Exception {
        String key = (String)headers.get(AmqpHeaders.RECEIVED_ROUTING_KEY);

        try {
            JuseMqHandler handler = receiverMapping.get(key);
            if (null != handler) {
                List<Object> args = new ArrayList<>();
                Type[] types = handler.getGenericParameterTypes();
                if (ArrayUtils.isNotEmpty(types)) {
                    Object args0 = message.getBody();
                    // JSON
                    String contentType = (String)headers.get(AmqpHeaders.CONTENT_TYPE);
                    if (null != contentType && contentType.indexOf("json") > 0) {
                        args0 = JSON.parseObject(message.getBody(), types[0]);
                    }
                    args.add(args0);
                    Map<String, Object> typeValues = new HashMap<>();
                    typeValues.put(Message.class.getName(), message);
                    typeValues.put(Channel.class.getName(), channel);
                    typeValues.put(headers.getClass().getName(), headers);
                    for (int i = 1; i < types.length; i++) {
                        args.add(typeValues.get(types[i].getTypeName()));
                    }
                }

                handler.invoke(args.stream().toArray());
            }
        } catch (Exception e) {

        }

        Long deliveryTag = (Long)headers.get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }
    /**
     * 对外发送消息的方法
     *
     * @param server
     * @param key
     * @param message
     * @throws Exception
     */
    public <T> void send(String server, String key, Object message) {
//        CorrelationData correlationData = new CorrelationData();
//        correlationData.setId(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("test", "senduser", message);
    }

}
