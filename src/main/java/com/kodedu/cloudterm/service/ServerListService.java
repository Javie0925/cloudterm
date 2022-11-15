package com.kodedu.cloudterm.service;

import com.kodedu.cloudterm.controller.vo.ServerVO;
import com.kodedu.cloudterm.dao.ServerListDao;
import com.kodedu.cloudterm.dao.entity.Server;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServerListService {

    @Resource
    private ServerListDao serverListDao;

    public List<ServerVO> getServerList() {
        List<Server> serverList = serverListDao.getServerList();
        List<ServerVO> voList = serverList.stream().map(s -> {
            ServerVO serverVO = new ServerVO();
            BeanUtils.copyProperties(s, serverVO);
            return serverVO;
        }).collect(Collectors.toList());
        return voList;
    }

    public ServerVO getById(String id) {
        Server server = serverListDao.findById(id);
        ServerVO serverVO = new ServerVO();
        BeanUtils.copyProperties(server, serverVO);
        return serverVO;
    }

    public void upsert(Server server) {
        serverListDao.upsert(server);
    }
}
