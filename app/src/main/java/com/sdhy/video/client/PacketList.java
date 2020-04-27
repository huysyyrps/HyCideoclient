package com.sdhy.video.client;

import java.util.ArrayList;
import java.util.List;

public class PacketList {
    private static Object lock = new Object();

    private List<PacketObject> dataList = new ArrayList<PacketObject>();

    public void push(PacketObject packObj) {
        synchronized (lock) {
            if (dataList.size() <= 0) {
                dataList.add(packObj);
                return;
            }

            int pos = 0;
            long packNum1 = packObj.seqNum;

            for (int i = dataList.size() - 1; i >= 0; i--) {
                PacketObject packObj2 = dataList.get(i);
                long packNum2 = packObj2.seqNum;

                if (packNum2 < packNum1) {
                    pos = i + 1;
                    break;
                } else if (packNum2 == packNum1) {
                    return;
                } else if (packNum2 > packNum1) {
                    if (i > 0) {
                        continue;
                    } else {
                        if (packNum2 - packNum1 > 1) {
                            pos = -1;
                        } else {
                            pos = 0;
                        }
                        break;
                    }
                }
            }

            if (pos < 0) {
                return;
            }

            dataList.add(pos, packObj);
        }
    }

    public PacketObject get() {
        PacketObject packObj = null;
        synchronized (lock) {
            if (dataList.size() > 0) {
                packObj = dataList.remove(0);
            }
        }
        return packObj;
    }

    public int count() {
        synchronized (lock) {
            return dataList.size();
        }
    }

    public void clear() {
        synchronized (lock) {
            dataList.clear();
        }
    }

}
