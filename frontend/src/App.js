import Lobby from "./component/Lobby";
import Chat from "./component/Chat";
import { StompContext,initStompClient } from "./context/socket";



function App() {

  return (
    <StompContext.Provider value={initStompClient()}>
      <Lobby />
      <Chat />
    </StompContext.Provider>
  );
}

export default App;
