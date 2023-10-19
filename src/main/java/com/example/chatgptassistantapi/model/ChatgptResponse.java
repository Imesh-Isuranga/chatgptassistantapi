package com.example.chatgptassistantapi.model;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatgptResponse {

    private List<Choice> choices;
}
