import React, { useState, useEffect } from "react";
import Lobby from "./component/Lobby";
import Chat from "./component/Chat";
import ErrorHandler from "./component/ErrorHandler";
import { StompSessionProvider } from "react-stomp-hooks";
import { createUseStyles, ThemeProvider } from "react-jss";

const APP_URL = "http://localhost:9000/4gewinnt";
const WS_URL = APP_URL + "/ws";

const theme = {
  accentBackgroundColor: "rgb(240 240 240)",
  accentColor: "rgb(30 30 30)",
  accentHighlightBackgroundColor: "rgb(200 200 200)",
  accentHighlightColor: "rgb(10 10 10)",
};

const buttonBase = (theme) => ({
  fontFamily: "inherit",
  fontSize: "inherit",
  textDecoration: "none",
  padding: "0 1em",
  mozPaddingStart: "calc(10px - 3px)",
  margin: "4px 0",
  height: "40px",
  boxShadow: "2px 2px 5px 1px rgb(0 0 0 / 30%)",
  border: "none",
  borderRadius: "3px",
  cursor: "pointer",
  lineHeight: "1.1",
  textAlign: "center",
  display: "flex",
  flexGrow: "0",
  alignItems: "center",
  justifyContent: "center",
  color: theme.accentColor,
  backgroundColor: theme.accentBackgroundColor,

  "&:hover": {
    backgroundColor: theme.accentColor,
    color: theme.accentBackgroundColor,
    cursor: "pointer",
  },
});

const textInputBase = {
  fontFamily: "inherit",
  fontSize: "inherit",
  backgroundColor: "transparent",
  padding: "0 1em 0 0.5em",
  mozPaddingStart: "calc(10px - 3px)",
  margin: "4px 0",
  height: "40px",
  boxShadow: "2px 2px 5px 1px rgb(0 0 0 / 30%)",
  border: "none",
  borderRadius: "3px",
  lineHeight: "1.1",
};

const useStyles = createUseStyles({
  "@global": {
    body: {
      margin: "0",
      fontFamily:
        "-apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',\n    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',\n    sans-serif",
      webkitFontSmoothing: "antialiased",
      mozOsxFontSmoothing: "grayscale",
      height: "100vh",
    },
    html: {
      height: "100%",
    },
    "#root": {
      position: "absolute",
      display: "flex",
      flexFlow: "column nowrap",
      top: 0,
      bottom: 0,
      left: 0,
      right: 0,
      overflowY: "hidden",
    },

    button: {
      extend: buttonBase(theme),
    },

    input: {
      '[type="text"], [type="password"], [type="number"]': {
        extend: textInputBase,
      },
      '[type="submit"], [type="reset"], [type="button"]': {
        extend: buttonBase(theme),
      },
    },
    select: {
      extend: textInputBase,
    },
    textarea: {
      extend: textInputBase,
      paddingTop: "0.6em",
      width: "100%",
    },
    ".flex-row": {
      display: "flex",
      flexFlow: "row nowrap",
      justifyContent: "center",
      alignItems: "center",
      "&> * + *": {
        marginLeft: "5px",
      },
    },
    ".layout": {
      display: "grid",
      gridTemplateColumns: "2fr 1fr",
      margin: "0 auto",
      height: "100%",
      gridGap: "5px",
    },
  },
});

function App() {
  useStyles(theme);

  const [isLoading, setLoading] = useState(true);
  const [csrfHeaders, setCsrfHeaders] = useState({});

  useEffect(() => {
    loadCsrfStompHeaders().then((headers) => {
      console.log("CSRF headers loaded", headers);
      setCsrfHeaders(headers);
      setLoading(false);
    });
  }, []);

  const loadCsrfStompHeaders = () => {
    return fetch(APP_URL + "/csrf")
      .then((response) => {
        if (response.ok) {
          return response.json();
        } else {
          throw new Error("Failed to fetch CSRF token");
        }
      })
      .then((csrfDetails) => {
        return {
          "X-CSRF-TOKEN": csrfDetails.token,
        };
      });
  };

  return isLoading ? 
  (
    <div>L&auml;dt...</div>
  ) : (
    <ThemeProvider theme={theme}>
      <StompSessionProvider
        url={WS_URL}
        connectHeaders={csrfHeaders}
        debug={(str) => console.log(str)}
      >
        <ErrorHandler />
        <div className="layout">
          <Lobby />
          <Chat />
        </div>
      </StompSessionProvider>
    </ThemeProvider>
  );
}

export default App;
