package com.dong.rpc.rpc;

import com.dong.rpc.entity.RPCRequest;
import com.dong.rpc.extension.Scope;
import com.dong.rpc.extension.Spi;

/**
 * @author caolidong
 * @date 2017/10/12.
 */
@Spi(scope = Scope.PROTOTYPE)
public interface LoadBalance<T> {

    /**
     * find approximate Invoker
     * @param request
     * @return
     */
    Invoker<T> select(RPCRequest request);

    void setCluster(Cluster<T> cluster);

}
