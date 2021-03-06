package com.dong.rpc.registry.impl;

import com.dong.rpc.rpc.ServiceRegistry;
import com.dong.rpc.util.Constant;
import com.github.zkclient.ZkClient;
import org.apache.log4j.Logger;

/**
 * 基于Zookeeper的服务注册
 *
 * @author caolidong
 * @date 17/6/26.
 */
public class ZKServiceRegistry implements ServiceRegistry {
    private static final Logger LOGGER = Logger.getLogger(ZKServiceRegistry.class);

    private String registryAddress;

    private ZkClient zkClient;

    public ZKServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    /**
     * 此处加同步只是为了测试方便
     *
     * @param serviceName
     * @param serviceAddress
     */
    @Override
    public synchronized void register(String serviceName, String serviceAddress) {
        if (!zkClient.exists(Constant.ZK_REGISTRY_PATH)) {
            zkClient.createPersistent(Constant.ZK_REGISTRY_PATH);
        }

        if (!zkClient.exists(Constant.ZK_DATA_PATH)) {
            zkClient.createPersistent(Constant.ZK_DATA_PATH);
        }
        String servicePath = String.format("%s/%s", Constant.ZK_DATA_PATH, serviceName);
        if (!zkClient.exists(servicePath)) {
            zkClient.createPersistent(servicePath);
        }

        String addressPath = String.format("%s/%s", servicePath, serviceAddress);
        zkClient.createEphemeral(addressPath, serviceAddress.getBytes());
        LOGGER.info("register service :" + addressPath);
    }

    @Override
    public void unregister(String serviceName, String serviceAddress) {
        String servicePath = String.format("%s/%s", Constant.ZK_DATA_PATH, serviceName);
        String addressPath = String.format("%s/%s", servicePath, serviceAddress);
        zkClient.delete(addressPath);
        LOGGER.info("unregister service :" + addressPath);
    }

    @Override
    public void open() {
        zkClient = new ZkClient(registryAddress);
    }

    @Override
    public void close() {
        zkClient.close();
    }
}
