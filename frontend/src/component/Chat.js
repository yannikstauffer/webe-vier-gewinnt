import React, { useContext, useEffect, useState } from "react";
import { StompContext } from "../context/socket";

const Chat = () => {
  const stompClient = useContext(StompContext);
  console.log(stompClient);

  const [privateChats, setPrivateChats] = useState(new Map());
  const [publicChats, setPublicChats] = useState([]);
  const [tab, setTab] = useState("LOBBY");
  const [chatState, setUserData] = useState({
    text: "",
    senderId: undefined,
    receiverId: undefined,
  });

  // entry point
  useEffect(() => {
    registerWSTopics();
  });

  const registerWSTopics = () => {
    // todo: ensure stompclient is connected when this is called
    stompClient.subscribe("/chat/lobby", onLobbyMessageReceived);
    stompClient.subscribe(
      "/user/" + chatState.username + "/chat",
      onPrivateMessageReceived
    );
  }

  const onLobbyMessageReceived = (payload) => {
    console.log("lobby message received", payload);

    var payloadData = JSON.parse(payload.body);
    publicChats.push(payloadData);
    setPublicChats([...publicChats]);
  };

  const onPrivateMessageReceived = (payload) => {
    console.log("private message received", payload);
    var payloadData = JSON.parse(payload.body);
    let list = [];
    list.push(payloadData);
    privateChats.set(payloadData.senderName, list);
    setPrivateChats(new Map(privateChats));
  };

  const onError = (err) => {
    console.log(err);
  };

  const sendMessage = () => {
    if (stompClient) {
      var newMessage = {
        text: chatState.text,
        receiverId: tab === "LOBBY" ? null : chatState.receiverId,
      };
      console.log("sending message", newMessage);
      stompClient.send("/4gewinnt/message", {}, JSON.stringify(newMessage));
      setUserData({ ...chatState, text: "" });
    }
  };

  const handleMessageInput = (event) => {
    const { value } = event.target;
    setUserData({ ...chatState, text: value });
  };

  return (
    <div className="container">
      <h1>Chat</h1>
      <div>
        <div className="chat-history"></div>
        <div className="chat-input">
          <input
            type="text"
            placeholder="Hier könnte ihre Nachricht stehen"
            value={chatState.text}
            onChange={handleMessageInput}
          />
          <button type="button" onClick={sendMessage}>
            Senden
          </button>
        </div>
      </div>
    </div>
  );
};

export default Chat;
