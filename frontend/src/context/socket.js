import React from "react";

import {over} from 'stompjs';
import SockJS from 'sockjs-client';
export const initStompClient = () => {
    let sockJS = new SockJS("http://localhost:9000/ws");
    let stompClient = over(sockJS);
    stompClient.connect(   );
    return stompClient;
  };
export const StompContext = React.createContext();