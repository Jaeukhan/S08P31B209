/* eslint-disable react-refresh/only-export-components */
import Label from "./Label";

import type { Meta, StoryObj } from "@storybook/react";

const meta: Meta<typeof Label> = {
  title: "Atoms/Label",
  component: Label,
  argTypes: {},
};

export default meta;
type Story = StoryObj<typeof Label>;

export const Primary: Story = {
  args: {
    text: "Label Test",
    fontSize: "md",
  },
};
