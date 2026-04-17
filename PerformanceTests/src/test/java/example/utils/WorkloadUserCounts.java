package example.utils;

import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class WorkloadUserCounts {

    private static final Map<DatasetSize, Map<WorkloadType, Integer>> USER_COUNTS = Map.of(
            DatasetSize.SMALL, Map.of(
                    WorkloadType.BASIC, 1,
                    WorkloadType.MIXED_TPC, 200,
                    WorkloadType.NEW_ORDER, 140,
                    WorkloadType.PAYMENT, 160,
                    WorkloadType.ORDER_STATUS, 90,
                    WorkloadType.DELIVERY, 70,
                    WorkloadType.STOCK_LEVEL, 90),
            DatasetSize.MEDIUM, Map.of(
                    WorkloadType.BASIC, 1,
                    WorkloadType.MIXED_TPC, 500,
                    WorkloadType.NEW_ORDER, 360,
                    WorkloadType.PAYMENT, 400,
                    WorkloadType.ORDER_STATUS, 220,
                    WorkloadType.DELIVERY, 180,
                    WorkloadType.STOCK_LEVEL, 220),
            DatasetSize.LARGE, Map.of(
                    WorkloadType.BASIC, 1,
                    WorkloadType.MIXED_TPC, 1000,
                    WorkloadType.NEW_ORDER, 720,
                    WorkloadType.PAYMENT, 800,
                    WorkloadType.ORDER_STATUS, 450,
                    WorkloadType.DELIVERY, 360,
                    WorkloadType.STOCK_LEVEL, 450));

    public static int userCountFor(DatasetSize datasetSize, WorkloadType workloadType) {
        return USER_COUNTS.get(datasetSize).get(workloadType);
    }
}
