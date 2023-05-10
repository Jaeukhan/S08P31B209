import tw, { styled } from "twin.macro";

const Wrapper = styled.div(tw`border border-black flex justify-center`);

const PasswordReset = () => {
  return (
    <>
      <Wrapper>
        <div>
          <Wrapper> 비밀번호 재설정 </Wrapper>
        </div>
      </Wrapper>
      <Wrapper>
        <div>
          <Wrapper> 비밀번호 입력(필수) </Wrapper>
          <Wrapper> 비밀번호확인 입력(필수) </Wrapper>
        </div>
      </Wrapper>
      <Wrapper>
        <div>
          <Wrapper> 비밀번호 변경 </Wrapper>
          <Wrapper> 회원가입 </Wrapper>
        </div>
      </Wrapper>
    </>
  );
};

export default PasswordReset;