package com.dong.rpc.proxy;

import com.dong.rpc.entity.RPCRequest;
import com.dong.rpc.entity.RPCResponse;
import com.dong.rpc.exception.TimeoutException;
import com.dong.rpc.exception.ToyException;
import com.dong.rpc.rpc.Cluster;
import com.dong.rpc.trace.RPCHolder;
import com.dong.rpc.trace.RPCTrace;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.UUID;

/**
 * @author caolidong
 * @date 2017/9/29.
 */
public class RequestInvocationHandler<T> implements InvocationHandler {

    private static Logger logger = Logger.getLogger(RequestInvocationHandler.class);

    private Class<T> clazz;

    private Cluster cluster;

    public RequestInvocationHandler(Class<T> clazz, Cluster cluster) {
        this.clazz = clazz;
        this.cluster = cluster;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isLocalMethod(method)) {
            throw new ToyException("local method should not be invoked:" + method.getName());
        }
        boolean hasTrace = RPCHolder.hasTrace();
        RPCTrace trace;
        if (!hasTrace) {
            //第一层调用初始化跟踪信息
            RPCHolder.init();
        } else {
            trace = RPCHolder.getTrace();
            trace.setRequestTime(Calendar.getInstance().getTime());
            trace.setSpan(UUID.randomUUID().toString());
        }
        RPCRequest rpcRequest = getRequest(clazz, method, args);
        RPCResponse rpcResponse = cluster.invoke(rpcRequest);
        if (rpcResponse == null) {
            throw new TimeoutException();
        }
        if (rpcResponse.getThrowable() != null) {
            throw rpcResponse.getThrowable();
        }
        trace = RPCHolder.getTrace();
        trace.setResponseTime(Calendar.getInstance().getTime());
        logger.info(trace);
        if (!hasTrace) {
            RPCHolder.remove();
        }
        return rpcResponse.getResponse();
    }

    private RPCRequest getRequest(Class<?> clazz, Method method, Object[] args) {
        RPCRequest rpcRequest = new RPCRequest();
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setClazz(clazz);
        rpcRequest.setMethod(method.getName());
        rpcRequest.setParams(args);
        rpcRequest.setParamTypes(method.getParameterTypes());
        rpcRequest.setRequestTime(Calendar.getInstance().getTime().getTime());
        rpcRequest.setTrace(RPCHolder.getTrace());
        return rpcRequest;
    }

    private boolean isLocalMethod(Method method) {
        if (method.getDeclaringClass().equals(Object.class)) {
            try {
                Method interfaceMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
                return false;
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }
}
