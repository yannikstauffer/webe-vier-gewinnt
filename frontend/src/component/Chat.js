import React, { useState } from "react";
import { useStompClient, useSubscription } from "react-stomp-hooks";

const Chat = () => {
  const stompClient = useStompClient();

  const [privateChats, setPrivateChats] = useState(new Map());
  const [lobbyMessages, setLobbyMessages] = useState([]);
  const [tab, setTab] = useState("LOBBY");
  const [chatState, setUserData] = useState({
    text: "",
    senderId: 1,
    receiverId: 2,
  });

  const onLobbyMessageReceived = (payload) => {
    console.log("lobby message received", payload);
    let payloadData = JSON.parse(payload.body);

    lobbyMessages.push(payloadData);
    setLobbyMessages([...lobbyMessages]);
  };

  const onPrivateMessageReceived = (payload) => {
    console.log("private message received", payload);
    let payloadData = JSON.parse(payload.body);
    
    let list = privateChats.get(payloadData.senderId);
    if(!list) list = [];
    list.push(payloadData);
    privateChats.set(payloadData.senderId, list);
    setPrivateChats(new Map(privateChats));
  };

  useSubscription("/chat/lobby", onLobbyMessageReceived);
  useSubscription("/user/" + chatState.senderId + "/chat", onPrivateMessageReceived);

  const sendMessage = () => {
    if (stompClient) {
      let newMessage = {
        text: chatState.text,
        receiverId: tab === "LOBBY" ? null : chatState.receiverId,
      };
      console.log("sending message", newMessage);
      stompClient.publish({
        destination: "/4gewinnt/message",
        body: JSON.stringify(newMessage)
      });
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
        <div className="chat-history">
          <ul>
            {lobbyMessages.map((message, index) => (
              <li key={index}>{message.text}</li>

                ))}
          </ul>
        </div>
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
