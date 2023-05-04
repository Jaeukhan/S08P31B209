import tw, { styled } from "twin.macro";

const Wrapper = styled.div(tw`border border-black flex justify-center`);

const FindPassword = () => {
  return (
    <>
      <Wrapper>
        <Wrapper>DocDocLogo</Wrapper>
      </Wrapper>
      <Wrapper>
        <div>
          <Wrapper> 비밀번호 찾기 </Wrapper>
        </div>
      </Wrapper>
      <Wrapper>
        <div>
          <Wrapper> 이메일 입력 </Wrapper>
          <Wrapper> 비밀번호 입력 </Wrapper>
          <Wrapper> 생년월일 입력 </Wrapper>
        </div>
      </Wrapper>
      <Wrapper>
        <div>
          <Wrapper> 비밀번호 재설정 </Wrapper>
          <Wrapper> 회원가입 </Wrapper>
        </div>
      </Wrapper>
    </>
  );
};

export default FindPassword;
