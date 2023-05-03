import StyledTableHeader from "./TableHeader.styles";
import { InputProps } from "./TableHeader.types";

import "twin.macro";

const TableHeader = ({ width, height, label }: InputProps) => {
  return (
    <StyledTableHeader width={width} height={height}>
      <span tw="text-base font-bold align-middle ml-12">{label}</span>
    </StyledTableHeader>
  );
};
export default TableHeader;
