import { styled } from "styled-components"

const ArticleA = styled.article`
  display: grid;
  grid-template-columns: max-content 1fr;
  margin-bottom: 24px;
`
const SpanVoteContainer = styled.span`
  margin-right: 16px;
`
const ButtonUpDown = styled.button`
  width: 45px;  height: 45px;
  border-radius: 50%;
  border: 1px solid rgb(214, 217, 220);
  padding: 10px;
  background-color: transparent;
  font-size: 13px;
  cursor: pointer;
`
const DivVote = styled.div`
  width: 45px; height: 45px;
  padding: 10px;
  text-align: center;
  font-size: 19px;
`
const SpanComment = styled.span`
  padding: 0 3px 2px;
  font-size: 13px;
  color: rgb(131, 140, 149);
  cursor: pointer;
`
const SpanQContainer = styled.span`
  margin-bottom: 16px;
`
const DivQText = styled.div`
`
const DivShareEditProfile = styled.div`
  margin: 16px 0;
  display: flex;
  justify-content: space-between;
`
const SpanShare = styled.span`
  color: rgb(106, 115, 124);
  font-size: 13px;
  cursor: pointer;
  & span{
    margin-right: 8px;
  }
`
const SpanProfile = styled.span`
  width: 207px; height: 67px;
  border-radius: 5px;
  padding: 7px;
  background-color: rgb(217, 234, 247);
  font-size: 13px;
  >:first-child{
    margin-bottom: 4px;
    color: rgb(106, 115, 124);
  }
`
const SpanProfileUser = styled.span`
  display: flex;
  >:first-child{
    margin-right: 8px;
  }
`
const DivUserName = styled.div`
  color: rgb(0, 116, 204);
`
const DivFollow = styled.div`
  color: rgb(106, 115, 124);
`

function Answer({answer, shareClick}){
    return(
      <ArticleA>
        <SpanVoteContainer>
          <ButtonUpDown>
            ▲
          </ButtonUpDown>
          <DivVote>
            {answer.voteUp.length - answer.voteDown.length}
          </DivVote>
          <ButtonUpDown>
            ▼
          </ButtonUpDown>
        </SpanVoteContainer>
        <SpanQContainer>
          <DivQText>
            {answer.body}
          </DivQText>
          <DivShareEditProfile>
            <SpanShare>
              <span onClick={shareClick}>
                Share
              </span>
              <span>
                Improve this question
              </span>
              <span>
                Follow
              </span>
            </SpanShare>
            <SpanProfile>
              <div>{"asked "+new Intl.DateTimeFormat("en-GB",{month: 'short', day: 'numeric'}).format(new Date(answer.created_at))}</div>
              <SpanProfileUser>
                <img
                  src={answer.avatarUrl}
                  alt=""
                  width="32px" height="32px"
                />
                <span>
                  <DivUserName>name</DivUserName>
                  <DivFollow>follow</DivFollow>
                </span>
              </SpanProfileUser>
            </SpanProfile>
          </DivShareEditProfile>
        </SpanQContainer>
        <span></span>
        <SpanComment>Add a comment</SpanComment>                
      </ArticleA>
    )
  }
  export default Answer;