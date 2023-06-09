import { addWidget } from "@store/slice/imageViewSlice";
import type { RootState } from "@store/store";

import Widget from "./Widget";

import { useEffect, useRef } from "react";
import { useDispatch, useSelector } from "react-redux";
import "twin.macro";

const ImageViewer = () => {
  const dispatch = useDispatch();
  const viewState = useSelector((state: RootState) => state.imageView);
  const fileState = useSelector((state: RootState) => state.imageView.file);

  const canvasFrameRef = useRef<HTMLDivElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);

  // 이미지가 교체되거나 이미지 크기가 바뀔때 새로 그리는 useEffect
  useEffect(() => {
    const canvas: HTMLCanvasElement | null = canvasRef.current;
    const context = canvas?.getContext("2d");

    if (fileState) {
      const img = new Image();
      img.src = fileState.data;
      img.onload = () => {
        // const width =
        //   img.width <= img.height
        //     ? img.width / (img.height / ((canvas?.height ?? 0) * 0.95))
        //     : (canvas?.width ?? 0) * 0.95;
        // const height =
        //   img.height < img.width
        //     ? img.height / (img.width / ((canvas?.width ?? 0) * 0.95))
        //     : (canvas?.height ?? 0) * 0.95;
        const width = img.width / (img.height / (canvas?.height ?? 0));
        const height = canvas?.height ?? 0;

        // const width = img.width;
        // const height = img.height;

        context?.clearRect(0, 0, canvas?.width ?? 0, canvas?.height ?? 0);
        context?.drawImage(
          img,
          ((canvas?.width ?? 0) - width * (viewState.zoom / 100)) / 2,
          ((canvas?.height ?? 0) - height * (viewState.zoom / 100)) / 2,
          width * (viewState.zoom / 100),
          height * (viewState.zoom / 100),
        );
      };
    }
  }, [fileState, viewState.zoom]);

  // 캔버스를 이미지로 다운로드 받는 메소드
  const downloadCanvas = () => {
    const canvasOrigin: HTMLCanvasElement = canvasRef.current
      ? canvasRef.current
      : document.createElement("canvas");

    const canvasWidget: HTMLCanvasElement = document.createElement("canvas");
    canvasWidget.width = window.outerWidth;
    canvasWidget.height = window.outerHeight;

    const ctxWidget = canvasWidget.getContext("2d");
    viewState.widgets.map((widget) => {
      if (widget.type === "text") {
        ctxWidget
          ? (ctxWidget.font =
              widget.attributes.fontSize + " " + widget.attributes.font)
          : null;

        // const offsetLeft = canvasOrigin.parentElement?.offsetLeft ?? 0;
        // const offsetTop = canvasOrigin.parentElement?.offsetTop ?? 0;
        const ratioX =
          window.outerWidth / (canvasOrigin.parentElement?.offsetWidth ?? 0);
        const ratioY =
          window.outerHeight / (canvasOrigin.parentElement?.offsetHeight ?? 0);
        // 텍스트 좌표 좌상단
        ctxWidget ? (ctxWidget.textBaseline = "top") : null;
        ctxWidget?.fillText(
          widget.value,
          ((widget.pos.x - (canvasOrigin.parentElement?.offsetWidth ?? 0) / 2) *
            (viewState.zoom / 100) +
            (canvasOrigin.parentElement?.offsetWidth ?? 0) / 2) *
            ratioX, //+ offsetLeft,
          ((widget.pos.y -
            (canvasOrigin.parentElement?.offsetHeight ?? 0) / 2) *
            (viewState.zoom / 100) +
            (canvasOrigin.parentElement?.offsetHeight ?? 0) / 2) *
            ratioY, //+ offsetTop,
        );
      }
    });

    const canvasCombine: HTMLCanvasElement = document.createElement("canvas");
    canvasCombine.width = window.outerWidth;
    canvasCombine.height = window.outerHeight;

    const ctxCombine = canvasCombine.getContext("2d");
    ctxCombine?.drawImage(canvasOrigin, 0, 0);
    ctxCombine?.drawImage(canvasWidget, 0, 0);

    const img = new Image();
    if (fileState) {
      img.src = fileState.data;
    }

    const width = img.width / (img.height / canvasOrigin.height);
    const height = canvasOrigin.height;

    const canvasOutput: HTMLCanvasElement = document.createElement("canvas");
    canvasOutput.width = width;
    canvasOutput.height = height;

    const ctxOutput = canvasOutput.getContext("2d");
    ctxOutput?.drawImage(
      canvasCombine,
      (canvasOrigin.width - width * (viewState.zoom / 100)) / 2,
      (canvasOrigin.height - height * (viewState.zoom / 100)) / 2,
      width * (viewState.zoom / 100),
      height * (viewState.zoom / 100),
      0,
      0,
      width,
      height,
    );

    const link = document.createElement("a");
    link.href = canvasOutput.toDataURL("image/png") ?? "#";
    link.download = viewState.name + ".png";

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <>
      <div ref={canvasFrameRef} tw="relative w-full h-full">
        <div tw="absolute z-10 flex justify-center">
          <button
            tw="mx-2 px-1 border border-black"
            onClick={() => {
              dispatch(
                addWidget({
                  idx: Math.random().toString(36),
                  name: "Name",
                  type: "text",
                  pos: { x: 0, y: 0 },
                  value: "Empty Widget",
                  attributes: {
                    font: "serif",
                    fontSize: "24px",
                  },
                }),
              );
            }}
          >
            Add Memo
          </button>
          <button
            tw="mx-2 px-1 border border-black"
            onClick={() => downloadCanvas()}
          >
            Save File
          </button>
        </div>
        <canvas
          ref={canvasRef}
          width={window.outerWidth}
          height={window.outerHeight}
          tw="w-full h-full bg-lightgray-300"
        />
        {viewState.widgets.map((widget) => (
          <Widget
            widget={widget}
            parent={canvasFrameRef.current}
            key={widget.idx}
            {...widget}
          />
        ))}
      </div>
    </>
  );
};

export default ImageViewer;
