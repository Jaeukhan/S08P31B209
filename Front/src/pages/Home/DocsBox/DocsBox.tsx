import { TableHeader } from "@/components/atoms";
import { TableViewHeader, TableViewRow } from "@/components/molecules";

import "twin.macro";

const DocsBox = () => {
  return (
    <>
      <div tw="border bg-orange-100 w-full h-full">
        <div tw="flex bg-lightgray-700 h-[3.5rem] items-center ml-[8rem] mr-[8rem]">
          <TableHeader
            label="내 보관함"
            width="40rem"
            height="3.5rem"
            tw="bg-orange-400"
            size="lg"
          ></TableHeader>
        </div>
        <div tw="flex flex-col h-full">
          <div tw="grow bg-orange-300">SearchBar</div>
          <div tw="grow bg-orange-400">TableViewHeader</div>
          <div tw="grow bg-orange-500">TableViewRow</div>
        </div>
      </div>
    </>
  );
};

export default DocsBox;
