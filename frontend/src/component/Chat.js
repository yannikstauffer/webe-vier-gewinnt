import React, {useState} from "react";
import {useStompClient, useSubscription} from "react-stomp-hooks";
import {useTranslation} from 'react-i18next';
import './Chat.css';

const LOBBY_TAB = "LOBBY";

const Chat = ({userId}) => {
    const {t, i18n} = useTranslation();
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

        addPrivateChat(chatPartner);

        let list = privateChats.get(chatPartner.id);
        list.push(privateMessagePayload);
        privateChats.set(chatPartner.id, list);
        setPrivateChats(new Map(privateChats));
    }

    const addPrivateChat = (user) => {
        if (privateChats.has(user.id)) return;
        privateChats.set(user.id, []);
        setPrivateChats(new Map(privateChats));
    }

    const onUsersReceived = (payload) => {
        console.debug("users received", payload);
        let usersPayload = JSON.parse(payload.body);
        usersPayload.forEach(user => updateUser(user, true));
    };

    const onUserUpdateReceived = (payload) => {
        console.debug("userUpdate received", payload);
        let userUpdatePayload = JSON.parse(payload.body);

        let user = userUpdatePayload.user;
        if (userUpdatePayload.updateType === "ONLINE") {
            updateUser(user, true);
        } else if (userUpdatePayload.updateType === "OFFLINE") {
            if (privateChats.has(user.id) && privateChats.get(user.id).length > 0) {
                updateUser(user, false);
            } else {
                removeUser(user);
            }
        }
    };

    const onChatsReceived = (payload) => {
        console.debug("chats received", payload);
        let chatsPayload = JSON.parse(payload.body);

        chatsPayload.privateMessages.forEach(addMessageToPrivateChat);
        chatsPayload.lobbyMessages.forEach(addMessageToLobby);
    };

    const updateUser = (user, isOnline) => {
        user.online = isOnline || false;

        if (user.online) {
            addPrivateChat(user);
        }

        users.set(user.id, user);
        setUsers(new Map(users));
    };

    const removeUser = (user) => {
        privateChats.delete(user.id);
        users.delete(user.id);

        setPrivateChats(new Map(privateChats));
        setUsers(new Map(users));
    };

    useSubscription("/user/queue/users", onUsersReceived);
    useSubscription("/topic/users", onUserUpdateReceived);
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

            setChatState({...chatState, text: ""});
        }
    };

    const handleMessageInput = (event) => {
        const {value} = event.target;
        setChatState({...chatState, text: value});
    };

    const getMessageStyles = (message) => {
        let baseStyles = "chat-message";
        if (message.sender.id === userId) return baseStyles + " self";
        return baseStyles;
    };

    const setTab = (user) => {
        console.debug("setting tab to", user);

        if (user === LOBBY_TAB)
            return setChatState({...chatState, tab: LOBBY_TAB});

        setChatState({...chatState, tab: user.id});
    };

    const getTabMessages = () => {
        if (chatState.tab === LOBBY_TAB) return lobbyMessages;
        return privateChats.get(chatState.tab);
    };

    const getTabStyles = (tab) => {
        const baseStyles = "chat-tab";
        if (chatState.tab === tab) return baseStyles + " selected";
        return baseStyles;
    };

    const getUserTabs = () => {
        const onlineState = (user) => {
            if (user.online) return "🟢";
            return "🔴";
        };

        return privateChats.size > 0 &&
            Array.from(privateChats.keys())
                .filter((chatUserId) => chatUserId !== userId)
                .filter((chatUserId) => users.has(chatUserId))
                .map((chatUserId) => users.get(chatUserId))
                .map((user) => (<li
                    key={user.id}
                    className={getTabStyles(user.id)}
                    onClick={() => setTab(user)}
                >
                    {onlineState(user)} {user.firstName}
                </li>));
    }

    const getMessageMetaData = (message) => {
        const sentAt = new Date(message.sentAt);

        const date = sentAt.toLocaleDateString();
        const time = sentAt.toLocaleTimeString();

        const dateTime = sentAt.getDate() !== new Date().getDate() ? date + " " + time : time;

        if (message.sender.id === userId) return dateTime;
        return message.sender.firstName + " " + dateTime;
    }

    return (
        <div className="chat-layout">
            <h2>{t('chat.title')}</h2>
            <ul className="chat-tabs">
                <li
                    key={LOBBY_TAB}
                    className={getTabStyles(LOBBY_TAB)}
                    onClick={() => setTab(LOBBY_TAB)}
                >
                    {t('chat.user.lobbyName')}
                </li>
                {getUserTabs()}
            </ul>
            <ul className="chat-history">
                {getTabMessages().map((message, index) => (
                    <li key={index} className={getMessageStyles(message)}>
                        <div>{message.text}</div>
                        <div className="chat-message-metadata">{getMessageMetaData(message)}</div>
                    </li>
                ))}
            </ul>

            <div className="flex-row">
        <textarea
            type="text"
            placeholder={t('chat.placeholder')}
            value={chatState.text}
            onChange={handleMessageInput}
        />
                <button type="button" onClick={sendMessage}>
                    {t('chat.send')}
                </button>
            </div>
        </div>
    );
};

export default Chat;
