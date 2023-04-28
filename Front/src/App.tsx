import { useState } from "react";
import { RouterProvider } from "react-router-dom";
import router from "@/routers/router";
import { store } from "@/store/store";
import { Provider } from "react-redux";

function App() {
  return (
    <>
      <Provider store={store}>
        <RouterProvider router={router} />
      </Provider>
    </>
  );
}

export default App;
