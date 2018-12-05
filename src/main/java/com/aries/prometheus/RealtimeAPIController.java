package com.aries.prometheus;

import com.aries.extension.starter.PluginController;
import com.aries.extension.util.LogUtil;
import com.aries.extension.util.PropertyUtil;
import com.aries.prometheus.utils.JenniferConnection;
import com.aries.prometheus.utils.Metrics;
import com.aries.prometheus.utils.PrometheusFormat;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


@Controller
public class RealtimeAPIController extends PluginController {
    private static final String URL = PropertyUtil.getValue("prometheus", "url", "http://127.0.0.1:7900");
    private static final String TOKEN = PropertyUtil.getValue("prometheus", "token", "");

    // 실행 시간을 기준으로 몇 분전을 시작 시간으로 할 것인가
    private final int MINUTE_BEOFRE = -2;
    // 몇 분후를 종료 시간으로 할 것인가.
    private final int MINUTE_GAP = 1;

    private final int MINUTE_INTERVAL = 1;

    private final String TIME_FORMAT = "yyyyMMddHHmm";
    private final SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);

    private final String DB_METRIC_DOMAIN_URL = URL + "/api/dbmetrics/domain?time_pattern=" + TIME_FORMAT + "&interval_minute=" + MINUTE_INTERVAL + "&token=" + TOKEN;

    private final String DB_METRIC_INSTANCE_URL = URL + "/api/dbmetrics/instance?time_pattern=" + TIME_FORMAT + "&interval_minute=" + MINUTE_INTERVAL + "&token=" + TOKEN;

    @RequestMapping(value = {"/realtimeapi/prometheus"}, method = RequestMethod.GET,produces = "text/plain; charset=utf-8")
    @ResponseBody
    public String getRealtimeDomainMerged(@RequestParam(required=false) short[] domain_id, HttpServletRequest request) throws IOException {

        // 도메인 필터를 위한 Set 컬렉션 설정
        Set<Short> domainSet = new HashSet<Short>();
        if(domain_id != null) {
            for (int i = 0; i < domain_id.length; i++) {
                domainSet.add(domain_id[i]);
            }
        }

        // Get all domains
        JenniferConnection jc = new JenniferConnection();
        JSONObject jsonObj = jc.getResponse(URL + "/api/realtime/domain?token=" + TOKEN);

        // For response text
        StringBuffer sb = new StringBuffer();
        JSONObject jsonObjInstance;
        JSONArray jsonArrInstance;

        if(jsonObj.has("result")) {
            JSONArray jsonArr = jsonObj.getJSONArray("result");

            // 최종 데이터 머지하기
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject objDomain = jsonArr.getJSONObject(i);
                short sid = (short) objDomain.getInt("domainId");
                // domain을 지정했으면 지정한 도메인만 처리하거나 미지정일 때는 전체 레스폰스의 도메인을 처리
                if (domainSet.size() == 0 || domainSet.contains(sid)) {
                    sb.append(PrometheusFormat.realtimeDomain(objDomain));

                    // for instances information
                    jsonObjInstance = jc.getResponse(URL + "/api/realtime/instance?token=" + TOKEN + "&domain_id=" + sid);

                    jsonArrInstance = jsonObjInstance.getJSONArray("result");
                    for (int j = 0; j < jsonArrInstance.length(); j++) {
                        sb.append(PrometheusFormat.realtimeInstance(objDomain,jsonArrInstance.getJSONObject(j)));
                    }
                }
            }

        } else {
            if(jsonObj.has("exception")) {
                return jsonObj.toString();
            }
        }
        return sb.toString();
    }

    @RequestMapping(value = {"/dbmetrics/prometheus"}, method = RequestMethod.GET,produces = "text/plain; charset=utf-8")
    @ResponseBody
    public String getDbMetricsDomainMerged(@RequestParam(required=false) short[] domain_id, HttpServletRequest request) throws IOException {

        Set<Short> domainSet = new HashSet<Short>();
        if(domain_id != null) {
            for (int i = 0; i < domain_id.length; i++) {
                domainSet.add(domain_id[i]);
            }
        }

        // dbmetric starttime and endtime
        Date targetTime = new Date();
        String startTime = sdf.format(DateUtils.addMinutes(targetTime, MINUTE_BEOFRE));
        String endTime = sdf.format(DateUtils.addMinutes(targetTime, MINUTE_BEOFRE + MINUTE_GAP));

        // 현재 움직이고 있는 도메인의 리스트를 가지고 온다.
        JenniferConnection jc = new JenniferConnection();
        JSONObject realDomainJsonObj = jc.getResponse(URL + "/api/realtime/domain?token=" + TOKEN);

        StringBuffer sb = new StringBuffer();
        JSONObject realInstancejsonObj;
        JSONArray jsonArrInstance;

        if(realDomainJsonObj.has("result")) {
            JSONArray realDomainJsonArr = realDomainJsonObj.getJSONArray("result");

            for (int i = 0; i < realDomainJsonArr.length(); i++) {
                JSONObject objRealDomain = realDomainJsonArr.getJSONObject(i);
                short sid = (short) objRealDomain.getInt("domainId");
                // domain을 지정했으면 지정한 도메인만 처리하거나 미지정일 때는 전체 레스폰스의 도메인을 처리
                if (domainSet.size() == 0 || domainSet.contains(sid)) {
                    StringBuffer tmpUrl;
                    String fixedUrl = DB_METRIC_DOMAIN_URL + "&start_time=" + startTime + "&end_time=" + endTime + "&domain_id=" + sid;

                    // DB_METRIC_DOMAIN_URL에 startTime과 endTime, metrics를 더한다.
                    JSONObject domainMetricsJson;
                    JSONArray domainJsonArr;
                    // 지정한 메트릭스를 가지고 루프
                    for(Metrics.metrics metrics: Metrics.metrics.values()){
                        tmpUrl = new StringBuffer();
                        tmpUrl.append(fixedUrl + "&metrics=" + metrics);
                        domainMetricsJson = jc.getResponse(tmpUrl.toString());

                        if(domainMetricsJson.has("result")) {
                            domainJsonArr = domainMetricsJson.getJSONArray("result");

                            // make string for prometheus for per Domain
                            sb.append(PrometheusFormat.dbMetricsDomain(metrics.toString(), MINUTE_INTERVAL, objRealDomain, domainJsonArr.getJSONObject(0)));
                        }

                    }

                    // for instances information
                    realInstancejsonObj = jc.getResponse(URL + "/api/realtime/instance?token=" + TOKEN + "&domain_id=" + sid);

                    JSONObject instanceMetricsJson;
                    JSONArray instanceJsonArr;
                    jsonArrInstance = realInstancejsonObj.getJSONArray("result");
                    for (int j = 0; j < jsonArrInstance.length(); j++) {
                        JSONObject objRealInstance = jsonArrInstance.getJSONObject(j);

                        // get logic from dbmetrics
                        fixedUrl = DB_METRIC_INSTANCE_URL + "&start_time=" + startTime + "&end_time=" + endTime + "&instance_id=" + objRealInstance.getInt("instanceId") + "&domain_id=" + sid;
                        for(Metrics.metrics metrics: Metrics.metrics.values()){
                            tmpUrl = new StringBuffer();
                            tmpUrl.append(fixedUrl + "&metrics=" + metrics);
                            instanceMetricsJson = jc.getResponse(tmpUrl.toString());
                            instanceJsonArr =  instanceMetricsJson.getJSONArray("result");

                            sb.append(PrometheusFormat.dbMetricsInstanc(metrics.toString(), MINUTE_INTERVAL, objRealDomain, objRealInstance, instanceJsonArr.getJSONObject(0)));
                        }

                    }
                }
            }

        } else {
            if(realDomainJsonObj.has("exception")) {
                return realDomainJsonObj.toString();
            }
        }
        return sb.toString();
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String, Object> handleException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("exception", e.toString());

        return result;
    }
}
