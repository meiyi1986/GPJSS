package yimei.jss.jobshop;

import java.util.List;

/**
 * Created by dyska on 7/05/17.
 */
public interface JSSInstance {
    Shop createShop();

    void resetShop(Shop shop);

    int getNumWorkCenters();

    int getNumJobs();

    List<Double> getWorkCenterReadyTimes();
}
