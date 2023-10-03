import React, { useState } from "react";
import { useStompClient, useSubscription } from "react-stomp-hooks";
import {createUseStyles, useTheme} from "react-jss";


const userId = Math.floor(Math.random() * 1000);

const useStyles = createUseStyles({
  layout: {
    display: "grid",
    gridTemplateRows: "auto 1fr auto",
    gridGap: "5px",
    margin: "0 auto",
    height: "100%",
    overflowY: "scroll"
  },

  history: {
    display: "flex",
    flexFlow: "column nowrap",
    justifyContent: "flex-start",
  
    margin: 0,
    listStyle: "none",
    border: "1px solid #ccc",
    borderRadius: "5px",
    padding: "10px",
    overflowY: "scroll",
    paddingInline: "10px",
  },
  
  message: {
    display: "flex",
    listStyleType: "none",
    border: "1px solid #ccc",
    borderRadius: "5px",
    padding: "10px",
    width: "80%",
    '&:not(:last-child)': {
      marginBottom: "10px",
    },
    '&.self': {
      alignSelf: "flex-end",
    },
  },
  
});

const Chat = () => {
  const theme = useTheme();
  const classes = useStyles(theme);
  const stompClient = useStompClient();

  const [privateChats, setPrivateChats] = useState(new Map());
  const [lobbyMessages, setLobbyMessages] = useState([]);
  const [tab, setTab] = useState("LOBBY");
  const [chatState, setChatState] = useState({
    text: "",
    senderId: userId,
    receiverId: tab,
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
  useSubscription("/player/" + chatState.senderId + "/chat", onPrivateMessageReceived);

  const sendMessage = () => {
    if (stompClient && chatState.text) {
      let receiver = chatState.receiverId === "LOBBY" ? null : chatState.receiverId;
      let destination = chatState.receiverId === "LOBBY" 
          ? "/4gewinnt/message"
          : "/user/" + receiver + "/chat";

      let newMessage = {
        text: chatState.text,
        sender: chatState.senderId,
        receiver: receiver,
      };
      console.log("sending message", newMessage);
      stompClient.publish({
        destination: destination,
        body: JSON.stringify(newMessage)
      });
    


      setChatState({ ...chatState, text: "" });
    }
  };

  const handleMessageInput = (event) => {
    const { value } = event.target;
    setChatState({ ...chatState, text: value });
  };


  const getMessageStyles = (message) => {
    let baseStyles = classes.message
    if(message.sender.id === chatState.senderId) return classes.message + " self";
    return baseStyles;
  };

  return (
    <div className={classes.layout}>
      <h1>Chat</h1>
      <ul className={classes.history}>
        {lobbyMessages.map((message, index) => (
          <li key={index} className={getMessageStyles(message)}>{message.text}</li>

            ))}
      </ul>

      <div className='flex-row'>
        <textarea
          type="text"
          placeholder="Hier könnte ihre Nachricht stehen..."
          value={chatState.text}
          onChange={handleMessageInput}
        />
        <button type="button" onClick={sendMessage}>
          Senden
        </button>
      </div>
    </div>
  );
};

export default Chat;
