package com.example.user1.mq;

import lombok.Data;
import org.apache.commons.lang.reflect.MethodUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

@Data
public class JuseMqHandler {
    private Object object;
    private Method method;

    public JuseMqHandler(Object object, Method method) {
        this.object = object;
        this.method = method;
    }

    public Type[] getGenericParameterTypes() {
       return method.getGenericParameterTypes();
    }

    public void invoke(Object... args) throws Exception {
        MethodUtils.invokeMethod(object, method.getName(), args);
    }

}
