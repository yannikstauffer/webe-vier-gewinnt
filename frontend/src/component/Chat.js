import React, { useState } from "react";
import { useStompClient, useSubscription } from "react-stomp-hooks";
import { createUseStyles, useTheme } from "react-jss";

const useStyles = createUseStyles(theme=> ({
    layout: {
      display: "grid",
      gridTemplateRows: "auto  auto 1fr auto",
      gridGap: "5px",
      margin: "0 auto",
      height: "100%",
      overflowY: "scroll",
    },

    history: {
      display: "flex",
      flexFlow: "column nowrap",
      justifyContent: "flex-start",
      gridGap: "10px",

      margin: 0,
      listStyle: "none",
      border: "1px solid #ccc",
      borderRadius: "5px",
      padding: "10px",
      overflowY: "scroll",
      paddingInline: "10px",
    },

    tabs: {
      display: "flex",
      flexFlow: "row nowrap",
      justifyContent: "flex-start",
      gridGap: "2px",

      margin: 0,
      listStyle: "none",
      overflowX: "scroll",
    },

    tab: {
      display: "flex",
      listStyleType: "none",
      border: "1px solid #ccc",
      borderRadius: "5px",
      padding: "5px",
      width: "80%",
      "&.selected": {
        backgroundColor: theme.accentBackgroundColor,
      },
    },

    message: {
      display: "flex",
      listStyleType: "none",
      border: "1px solid #ccc",
      borderRadius: "5px",
      padding: "10px",
      width: "80%",
      "&.self": {
        alignSelf: "flex-end",
      },
    },
  }));

const LOBBY_TAB = "LOBBY";

const Chat = ({ userId }) => {
  const theme = useTheme();
  const classes = useStyles(theme);
  const stompClient = useStompClient();

  const [privateChats, setPrivateChats] = useState(new Map());
  const [users, setUsers] = useState(new Map());
  const [lobbyMessages, setLobbyMessages] = useState([]);

  const [chatState, setChatState] = useState({
    text: "",
    tab: LOBBY_TAB,
  });

  const onLobbyMessageReceived = (payload) => {
    console.debug("lobby message received", payload);
    let payloadData = JSON.parse(payload.body);

    addMessageToLobby(payloadData);
  };

  const onPrivateMessageReceived = (payload) => {
    console.debug("private message received", payload);
    let privateMessagePayload = JSON.parse(payload.body);

    addMessageToPrivateChat(privateMessagePayload);
  };

  const addMessageToLobby = (lobbyMessagePayload) => {
    lobbyMessages.push(lobbyMessagePayload);
    setLobbyMessages([...lobbyMessages]);
  }

  const addMessageToPrivateChat = (privateMessagePayload) => {
    let chatPartner = privateMessagePayload.sender.id === userId ?
        privateMessagePayload.receiver : privateMessagePayload.sender;

    addUser(chatPartner);

    let list = privateChats.get(chatPartner.id);
    list.push(privateMessagePayload);
    privateChats.set(chatPartner.id, list);
    setPrivateChats(new Map(privateChats));
  }
  const onUsersReceived = (payload) => {
    console.debug("users received", payload);
    let usersPayload = JSON.parse(payload.body);

    usersPayload.users.forEach(addUser);
  };

  const onChatsReceived = (payload) => {
    console.debug("chats received", payload);
    let chatsPayload = JSON.parse(payload.body);

    chatsPayload.privateMessages.forEach(addMessageToPrivateChat);
    chatsPayload.lobbyMessages.forEach(addMessageToLobby);
  };

  const addUser = (user) => {
    privateChats.has(user.id) || privateChats.set(user.id, []);
    users.set(user.id, user);

    setPrivateChats(new Map(privateChats));
    setUsers(new Map(users));
  };

  useSubscription("/topic/users", onUsersReceived);
  useSubscription("/user/queue/chats", onChatsReceived);
  useSubscription("/topic/lobby/chat", onLobbyMessageReceived);
  useSubscription("/user/queue/chat", onPrivateMessageReceived);

  const sendMessage = () => {
    if (stompClient && chatState.text) {
      let receiver = chatState.tab === LOBBY_TAB ? null : chatState.tab;
      let destination =
        receiver === null ? "/4gewinnt/message" : "/4gewinnt/private-message";

      let newMessage = {
        text: chatState.text,
        receiverId: receiver,
      };

      console.log("sending message", newMessage);
      stompClient.publish({
        destination: destination,
        body: JSON.stringify(newMessage),
      });

      setChatState({ ...chatState, text: "" });
    }
  };

  const handleMessageInput = (event) => {
    const { value } = event.target;
    setChatState({ ...chatState, text: value });
  };

  const getMessageStyles = (message) => {
    let baseStyles = classes.message;
    if (message.sender.id === userId) return classes.message + " self";
    return baseStyles;
  };

  const setTab = (user) => {
    console.debug("setting tab to", user);

    if (user === LOBBY_TAB)
      return setChatState({ ...chatState, tab: LOBBY_TAB });

    setChatState({ ...chatState, tab: user.id });
  };

  const getTabMessages = () => {
    if (chatState.tab === LOBBY_TAB) return lobbyMessages;
    return privateChats.get(chatState.tab);
  };

  const getTabStyles = (tab) => {
    let baseStyles = classes.tab;
    if (chatState.tab === tab) return classes.tab + " selected";
    return baseStyles;
  };

  const getUserTabs = () => {
    return privateChats.size > 0 &&
      Array.from(privateChats.keys())
      .filter((chatUserId) => chatUserId !== userId)
      .map((chatUserId) => (<li
          key={chatUserId}
          className={getTabStyles(chatUserId)}
          onClick={() => setTab(users.get(chatUserId))}
        >
          {users.get(chatUserId)?.firstName}
        </li>));
  }

  return (
    <div className={classes.layout}>
      <h2>Chat</h2>
      <ul className={classes.tabs}>
        <li
          key={LOBBY_TAB}
          className={getTabStyles(LOBBY_TAB)}
          onClick={() => setTab(LOBBY_TAB)}
        >
          Lobby
        </li>
        {getUserTabs()}
      </ul>
      <ul className={classes.history}>
        {getTabMessages().map((message, index) => (
          <li key={index} className={getMessageStyles(message)}>
            {message.text}
          </li>
        ))}
      </ul>

      <div className="flex-row">
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
