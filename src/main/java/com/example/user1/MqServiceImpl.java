package com.example.user1;

import com.example.user1.dto.UserDto;
import com.example.user1.mq.JuseMqReceiver;
import org.springframework.stereotype.Service;

@Service
@JuseMqReceiver
public class MqServiceImpl implements MqService {
    @JuseMqReceiver("user.senderuser")
    @Override
    public Boolean reciverUser(UserDto dto) {
        System.out.println("-----------------------"+dto.getUserName()+dto.getAge());
        return true;
    }
}
