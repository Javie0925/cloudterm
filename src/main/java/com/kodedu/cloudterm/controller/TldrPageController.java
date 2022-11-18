package com.kodedu.cloudterm.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.kodedu.cloudterm.helper.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/tldr")
@ResponseBody
@Controller
public class TldrPageController {

    Map<String, Map<String, JSONObject>> filePathMap = new HashMap<>();

    @GetMapping("/cmd/{cmd}")
    public Result getCmdDetail(@PathVariable String cmd) throws IOException {

        for (Map.Entry<String, Map<String, JSONObject>> entry : filePathMap.entrySet()) {
            if (entry.getValue().containsKey(cmd)) {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("tldr_pages/" + entry.getKey() + "/" + cmd + ".md");
                if (Objects.isNull(inputStream)) {
                    return Result.fail(String.format("command [%s] doesn't exist!", cmd));
                }
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                return Result.success(new String(bytes));
            }
        }
        return Result.fail("command [%s] doesn't exist!");
    }

    @GetMapping("/category/{category}")
    @ResponseBody
    public Result getCmdListByCategory(@PathVariable("category") String category) {
        if (filePathMap.containsKey(category)) {
            List<JSONObject> list = filePathMap.get(category).entrySet()
                    .stream()
                    .map(en -> en.getValue())
                    .sorted(Comparator.comparing(o -> o.getString("cmd")))
                    .collect(Collectors.toList());
            return Result.success(list);
        }
        return Result.fail(String.format("command category [%s] doesn't exist!", category));
    }

    @GetMapping("/{category}")
    public Result searchCmds(@PathVariable("category") String category, String prefix) {
        if (filePathMap.containsKey(category)) {
            List<JSONObject> list = filePathMap.get(category).entrySet()
                    .stream()
                    .filter(en -> en.getKey().toLowerCase().startsWith(prefix.toLowerCase()))
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .map(en -> en.getValue())
                    .collect(Collectors.toList());
            return Result.success(list);
        }
        return Result.fail(String.format("command category [%s] doesn't exist!", category));
    }

    @PostConstruct
    public void init() throws IOException {
        InputStream resource = getClass().getClassLoader().getResourceAsStream("tldr_pages/structure.json");
        if (Objects.isNull(resource)) return;
        byte[] bytes = new byte[resource.available()];
        resource.read(bytes);
        filePathMap = JSON.parseObject(new String(bytes), new TypeReference<>() {
        });
    }

    /**
     * init structure.json
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String basePath = "D:\\Workstation\\projects\\cloudterm\\src\\main\\resources\\tldr_pages";
        File file = new File(basePath);
        JSONObject obj = new JSONObject();
        for (File dir : file.listFiles()) {
            if (!dir.isDirectory()) continue;
            HashMap<Object, Object> tmpMap = new HashMap<>();
            for (File md : dir.listFiles()) {
                JSONObject mdObj = new JSONObject();
                mdObj.fluentPut("cmd", md.getName().replace(".md", ""));
                FileInputStream mdInStream = new FileInputStream(md);
                Scanner scanner = new Scanner(mdInStream);
                String line = null;
                do {
                    line = scanner.nextLine();
                } while (!line.contains(">"));
                mdObj.put("info", line.substring(2));
                tmpMap.put(md.getName().replace(".md", ""), mdObj);
            }
            obj.put(dir.getName(), tmpMap);
        }
        File structure = new File(file, "structure.json");
        if (!structure.exists()) {
            structure.createNewFile();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(structure);
        fileOutputStream.write(obj.toJSONString().getBytes(StandardCharsets.UTF_8));
        fileOutputStream.close();
    }
}
