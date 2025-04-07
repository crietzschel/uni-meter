package com.deigmueller.uni_meter.common.shelly;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RpcError {
  public static final int ERROR_NO_POWER_DATA = 1;
  public static final String ERROR_NO_POWER_DATA_MSG = "power data is currently not available (no data received from the input device)";
}
