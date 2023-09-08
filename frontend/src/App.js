import React from "react";
import Lobby from "./component/Lobby";
import Chat from "./component/Chat";
import { StompSessionProvider } from "react-stomp-hooks";

const WS_URL = "http://localhost:9000/ws";
function App() {
  return (
    <StompSessionProvider 
      url={WS_URL}
      debug={(str) => console.log(str)}>
      <Lobby />
      <Chat />
    </StompSessionProvider>
  );
}

export default App;
