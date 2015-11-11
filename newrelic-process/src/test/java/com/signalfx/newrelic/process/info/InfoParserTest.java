/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.process.info;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

public class InfoParserTest {

    @Test
    public void testParseInfo() throws Exception {
        String[] apis = {
                "applications",
                "servers"
        };
        AppInfo expectedConfig = new AppInfo(Arrays.asList(apis));

        String jsonString = IOUtils.toString(getClass().getResourceAsStream("/metrics.json"));
        AppInfo actualConfig = InfoParser.parseInfo(jsonString);

        assertEquals(expectedConfig, actualConfig);
    }
}
