package com.example.study_mdns;

import android.net.nsd.NsdServiceInfo;
import android.text.TextUtils;

import java.net.InetSocketAddress;
import java.util.Map;

public class NSDInfo {

    public static final String ASSISTANT = "ASSISTANT";
    public static final String MODEL_TYPE = "Model_Type";
    public static final String IPV4 = "ipv4";
    public static final String IPV6 = "ipv6";
    public final static int ADDED = 1;
    public final static int REMOVED = 2;

    private InetSocketAddress ipAddress;
    private String uuid;
    private int modelType;
    private int status;
    private boolean isAssistant;
    private boolean passed;

    public String getUuid() {
        return uuid;
    }

    public void setData(NsdServiceInfo serviceInfo) {
        this.uuid = serviceInfo.getServiceName();
        this.status = ADDED;

        Map<String, byte[]> attribute = serviceInfo.getAttributes();
        if (!attribute.isEmpty()) {
            if (attribute.containsKey(ASSISTANT)) {
                String assistant = new String(attribute.get(ASSISTANT));
                this.isAssistant = Boolean.parseBoolean(assistant);
            }

            if (attribute.containsKey(MODEL_TYPE)) {
                String type = new String(attribute.get(MODEL_TYPE));
                modelType = Integer.parseInt(type);
            }
        }

        if (this.ipAddress == null && serviceInfo.getHost() != null) {
            this.ipAddress = new InetSocketAddress(serviceInfo.getHost(), serviceInfo.getPort());
        }
    }
}
