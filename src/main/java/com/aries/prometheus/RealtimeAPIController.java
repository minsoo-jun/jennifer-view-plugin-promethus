package com.aries.prometheus;

import com.aries.extension.starter.PluginController;
import com.aries.extension.util.PropertyUtil;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class RealtimeAPIController extends PluginController {
    private static final String URL = PropertyUtil.getValue("realtimeapi", "url", "http://127.0.0.1:7900");
    private static final String TOKEN = PropertyUtil.getValue("realtimeapi", "token", "");

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @RequestMapping(value = {"/realtimeapi/prometheus"}, method = RequestMethod.GET,produces = "text/plain; charset=utf-8")
    @ResponseBody
    public String getDomainMerged(@RequestParam(required=false) short[] domain_id, HttpServletRequest request) throws IOException {

        // 도메인 필터를 위한 Set 컬렉션 설정
        Set<Short> domainSet = new HashSet<Short>();
        if(domain_id != null) {
            for (int i = 0; i < domain_id.length; i++) {
                domainSet.add(domain_id[i]);
            }
        }

        // Get all domains
        URL url = new URL(URL + "/api/realtime/domain?token=" + TOKEN);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // 응답 문자열 JSON 객체로 파싱
        InputStream in = new BufferedInputStream(conn.getInputStream());
        String jsonStr = IOUtils.toString(in, "UTF-8");
        JSONObject jsonObj = new JSONObject(jsonStr);
        // httpClient 객체 닫기
        conn.disconnect();

        // For response text
        StringBuffer sb = new StringBuffer();
        String jsonStrInstance;
        JSONObject jsonObjInstance;
        JSONArray jsonArrInstance;
        final String newline = "\n";

        if(jsonObj.has("result")) {
            JSONArray jsonArr = jsonObj.getJSONArray("result");

            // 최종 데이터 머지하기
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject obj = jsonArr.getJSONObject(i);
                short sid = (short) obj.getInt("domainId");
                // domain을 지정했으면 지정한 도메인만 처리하거나 미지정일 때는 전체 레스폰스의 도메인을 처리
                if (domainSet.size() == 0 || domainSet.contains(sid)) {

                    // Make text for prometheus
                    sb.append("# HELP jennifer_domain_tps Domain Total Transactions per second").append(newline);
                    sb.append("# TYPE jennifer_domain_tps gauge").append(newline);
                    sb.append("jennifer_domain_tps{application=\"" + obj.getString("domainName") + "\", domain_id=\"" + obj.getInt("domainId") +"\"} " + obj.getDouble("tps")).append(newline);

                    sb.append("# HELP jennifer_domain_response_time_milliseconds The average response time within the applicable range can be retrieved.").append(newline);
                    sb.append("# TYPE jennifer_domain_response_time_milliseconds gauge").append(newline);
                    sb.append("jennifer_domain_response_time_milliseconds{application=\"" + obj.getString("domainName") + "\", domain_id=\"" + obj.getInt("domainId") +"\"} " + obj.getDouble("responseTime")).append(newline);

                    sb.append("# HELP jennifer_domain_active_service_total Indicates the number of active services at the moment of collection; in other words, the number of transactions in operation.").append(newline);
                    sb.append("# TYPE jennifer_domain_active_service_total counter").append(newline);
                    sb.append("jennifer_domain_active_service_total{application=\"" + obj.getString("domainName") + "\", domain_id=\"" + obj.getInt("domainId") +"\"} " + obj.getInt("activeService")).append(newline);

                    sb.append("# HELP jennifer_domain_hit_hour Total time taken for applicable Calls").append(newline);
                    sb.append("# TYPE jennifer_domain_hit_hour counter").append(newline);
                    sb.append("jennifer_domain_hit_hour{application=\"" + obj.getString("domainName") + "\", domain_id=\"" + obj.getInt("domainId") +"\"} " + obj.getInt("hitHour")).append(newline);

                    // get each instance instance
                    // http://mon-trjensearch201z.prod.jp.local:7900/api/realtime/instance?token=kJ9Jnz2C3r2&domain_id=100
                    // for instances information
                    url = new URL(URL + "/api/realtime/instance?token=" + TOKEN + "&domain_id=" + sid);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    // 응답 문자열 JSON 객체로 파싱
                    in = new BufferedInputStream(conn.getInputStream());
                    jsonStrInstance = IOUtils.toString(in, "UTF-8");
                    jsonObjInstance = new JSONObject(jsonStrInstance);
                    // httpClient 객체 닫기
                    conn.disconnect();

                    jsonArrInstance = jsonObjInstance.getJSONArray("result");
                    for (int j = 0; j < jsonArrInstance.length(); j++) {
                        JSONObject objIns = jsonArrInstance.getJSONObject(j);
                        // Make text for prometheus
                        sb.append("# HELP jennifer_instance_tps instance Total Transactions per second").append(newline);
                        sb.append("# TYPE jennifer_instance_tps gauge").append(newline);
                        sb.append("jennifer_instance_tps{application=\"" + obj.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId") + "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getDouble("tps")).append(newline);

                        sb.append("# HELP jennifer_instance_response_time_milliseconds The average response time within the applicable range can be retrieved.").append(newline);
                        sb.append("# TYPE jennifer_instance_response_time_milliseconds gauge").append(newline);
                        sb.append("jennifer_instance_response_time_milliseconds{application=\"" + obj.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getDouble("responseTime")).append(newline);

                        //activeService
                        sb.append("# HELP jennifer_instance_active_service Indicates the number of active services at the moment of collection").append(newline);
                        sb.append("# TYPE jennifer_instance_active_service counter").append(newline);
                        sb.append("jennifer_instance_active_service{application=\"" + obj.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getDouble("activeService")).append(newline);

                        //concurrentUser
                        sb.append("# HELP jennifer_instance_concurrent_user Indicates the number of people currently using ").append(newline);
                        sb.append("# TYPE jennifer_instance_concurrent_user counter").append(newline);
                        sb.append("jennifer_instance_concurrent_user{application=\"" + obj.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getDouble("concurrentUser")).append(newline);

                        //procCPU
                        sb.append("# HELP jennifer_instance_process_cpu Indicates the CPU usage rate of a process being monitored").append(newline);
                        sb.append("# TYPE jennifer_instance_process_cpu gauge").append(newline);
                        sb.append("jennifer_instance_process_cpu{application=\"" + obj.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getDouble("procCPU")).append(newline);

                        //procMemory
                        sb.append("# HELP jennifer_instance_process_memory_bytes Indicates the memory usage by the process being monitored.").append(newline);
                        sb.append("# TYPE jennifer_instance_process_memory_bytes gauge").append(newline);
                        sb.append("jennifer_instance_process_memory_bytes{application=\"" + obj.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getDouble("procMemory")).append(newline);

                        //heapCommitted //heapUsed
                        sb.append("# HELP jennifer_instance_heap_memory_bytes Indicates the size of memory allocated to the heap memory area in the VM.").append(newline);
                        sb.append("# TYPE jennifer_instance_heap_memory_bytes gauge").append(newline);
                        sb.append("jennifer_instance_heap_memory_bytes{application=\"" + obj.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getDouble("heapUsed")).append(newline);

                        double usage = (objIns.getDouble("heapUsed") / objIns.getDouble("heapCommitted")) * 100 ;
                        //heapCommitted //heapUsed
                        sb.append("# HELP jennifer_instance_heap_memory_bytes Memory usage heapUsed/heapCommitted * 100.").append(newline);
                        sb.append("# TYPE jennifer_instance_heap_memory_bytes gauge").append(newline);
                        sb.append("jennifer_instance_heap_memory_usage{application=\"" + obj.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + usage).append(newline);

                        //hitHour
                        sb.append("# HELP jennifer_instance_visit_hour Total time taken for applicable Calls").append(newline);
                        sb.append("# TYPE jennifer_instance_visit_hour gauge").append(newline);
                        sb.append("jennifer_instance_process_memory_bytes{application=\"" + obj.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getInt("hitHour")).append(newline);
                    }

                }
            }

        } else {
            if(jsonObj.has("exception")) {
                return sb.toString();
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
