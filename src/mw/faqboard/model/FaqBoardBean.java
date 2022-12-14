package mw.faqboard.model;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import mw.faqboard.model.FaqBoardDAO;
import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

import java.lang.Integer;

@Controller
public class FaqBoardBean {
	
	@Autowired
	private FaqBoardDAO dao = null;
	
	//댓글 DAO
	@Autowired
	private FaqReplyDAO reply = null; 

	// FAQ 게시판,유저게시판 출력
	@RequestMapping("faqList.mw")
	public String faq_getArticles(FaqBoardDTO dto, FaqMainBoardDTO dto1, HttpSession session,
			HttpServletRequest respons, HttpServletRequest request, Model model) {
		List qList = null;

		qList = dao.selectMainFaq(dto1);
		int qcount = dao.getQcount(dto1);
		List<FaqReplyDTO> replycount = reply.faqContentReplyCount(); //댓글개수

		if (qcount > 0) {
			model.addAttribute("qList", qList);
			model.addAttribute("qcount", qcount);
			model.addAttribute("replycount", replycount);
		}

		int pageSize = 10;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		session.getAttribute("memId");
		// 페이지 수 지정
		String pageNum = request.getParameter("pageNum");
		if (pageNum == null) {
			pageNum = "1";
		} // 페이지 시작과 끝 객체 생성
		int currentPage = Integer.parseInt(pageNum);
		int start = (currentPage - 1) * pageSize + 1;
		int end = currentPage * pageSize;
		int number = 0;

		List articleList = null;

		int count = dao.getCount(dto);//전체게시글 개수 가져오기

		if (count > 0) {
			articleList = dao.getArticles(start, end);//SQL 에서 오름차순으로 데이터 찾는 메서드
		}

		number = count - (currentPage - 1) * pageSize; //초기값 0 으로 지정하여 전체글갯수에서 연산함

		if (count > 0) { //전체글 개수가 1개이상일때 실행
			int pageCount = count / pageSize + (count % pageSize == 0 ? 0 : 1);// (/)는 나누어서 나온값 + (%)는 나눠서 남은 값 0이 나온다면 트루 그외의 값이나온다면 1
			int startPage = (int) (currentPage / 10) * 10 + 1;
			int pageBlock = 10;
			int endPage = startPage + pageBlock - 1;
				if (endPage > pageCount) {
					endPage = pageCount;
				}
				model.addAttribute("startPage", startPage);
				model.addAttribute("endPage", endPage);
				model.addAttribute("pageCount", pageCount);
				model.addAttribute("startPage", startPage);
				model.addAttribute("endPage", endPage);
			}
			model.addAttribute("currentPage", currentPage);
			model.addAttribute("start", start);
			model.addAttribute("end", end);
			model.addAttribute("number", number);
			model.addAttribute("count", count);
			model.addAttribute("articleList", articleList);
		return "/faqboard/faqList";
	}

	
	@RequestMapping("faqDeleteForm.mw") // 유저 게시판 삭제
	public String faqDeleteForm(HttpServletRequest respons, ServletRequest request, Model model) {
		String pageNum = request.getParameter("pageNum"); 
		String faq_num = request.getParameter("faq_num");

		model.addAttribute("pageNum", pageNum); //취소를 눌렀을시에 돌아갈수있게 페이지번호를 보냄
		model.addAttribute("faq_num", faq_num);

		return "/faqboard/faqDeleteForm";
	}

	@RequestMapping("faqDeletePro.mw") // 유저게시판 삭제 진행
	public String faqDeletePro(HttpServletRequest respons, ServletRequest request, Model model) {
		String faq_num = request.getParameter("faq_num");
		String pw = request.getParameter("pw");

		int check = dao.DeleteCheck(faq_num, pw); //게시글번호와 비밀번호가 일치하면 삭제 진행

		model.addAttribute("check", check);
		model.addAttribute("faq_num", faq_num);
		model.addAttribute("pw", pw);

		if (check == 1) {
			dao.DeleteWriting(faq_num);
		}
		return "/faqboard/faqDeletePro";
	}
	@RequestMapping("faqAdminDelForm.mw") // 관리자 게시글 삭제
	public String faqAdminDelForm(Model model,ServletRequest request) {
		String faq_num =request.getParameter("faq_num");
		model.addAttribute("faq_num", faq_num);
		
		return "/faqboard/faqAdminDelForm";
	}
	
	@RequestMapping("faqAdminDel.mw") // 관리자 게시글 삭제 진행
	public String faqAdminDel(Model model,HttpSession session,ServletRequest request) {
		int faq_num =Integer.parseInt(request.getParameter("faq_num"));
		String id=request.getParameter("memId");
		
			dao.DeleteAdminfaq(faq_num); //게시글 번호로 찾아서 삭제
		
		
		return "/faqboard/faqAdminDel";
	}
	
	
	@RequestMapping("faqUpdateForm.mw") // 유저게시판 수정
	public String faqUpdateForm(FaqBoardDTO dto, HttpSession session, ServletRequest request, Model model) {
		String faq_num = request.getParameter("faq_num");
		String pageNum = request.getParameter("pageNum");

		FaqBoardDTO dto1 = dao.updateSelect(faq_num);//dto 객체에서 get 사용을 위해 DTO로 리턴받음

		model.addAttribute("pageNum", pageNum);
		model.addAttribute("faq_num", faq_num);
		model.addAttribute("dto1", dto1);

		return "/faqboard/faqUpdateForm";
	}

	@RequestMapping("faqUpdatePro.mw") // 유저게시판 수정진행
	public String faqUpdatePro(FaqBoardDTO dto1, ServletRequest request, Model model, String article) {
		String faq_num = request.getParameter("faq_num");
		String pw = request.getParameter("pw");
		
		int check = dao.updateCheck(faq_num, pw);// 2개의 객체를 비교하여 체크객체로 리턴받음

		model.addAttribute("check", check);
		model.addAttribute("faq_num", faq_num);
		model.addAttribute("pw", pw);

		if (check == 1) { //check 로 받은 int값이 조건과 일치한다면 실행
			dao.updateContent(dto1);
		}

		return "/faqboard/faqUpdatePro";
	}

	@RequestMapping("faqWriteForm.mw") // 유저게시판 글쓰기
	public String faqWriteForm(Model model, FaqBoardDTO dto, HttpServletRequest request, HttpServletResponse response,
			HttpSession session) {

		int faq_num = 0, readcount = 0;

		model.addAttribute("faq_num", faq_num);
		model.addAttribute("readcount", readcount);

		return "/faqboard/faqWriteForm";
	}

	@RequestMapping("faqWritePro.mw") // 유저게시판 글쓰기 실행
	public String faqWritePro(FaqBoardDTO dto, HttpSession session) {

		String id = (String) session.getAttribute("memId");
		dto.setId(id);

		dao.insertBoard(dto);
		return "/faqboard/faqWritePro";
	}

	@RequestMapping("myList.mw") // 내가 쓴글 보기
	public String myList(HttpSession session, HttpServletRequest respons, HttpServletRequest request, Model model) {

		int pageSize = 10;

		String pageNum = request.getParameter("pageNum");
		String id = (String) session.getAttribute("memId");

		if (pageNum == null) {
			pageNum = "1";
		}
		int currentPage = Integer.parseInt(pageNum);
		int start = (currentPage - 1) * pageSize + 1;
		int end = currentPage * pageSize;

		int number = 0;

		List articleList = null;

		int count = dao.getCountmy(id);
		if (count > 0) {
			articleList = dao.getArticles(start, end, id);
		}
		number = count - (currentPage - 1) * pageSize;

		int pageCount = count / pageSize + (count % pageSize == 0 ? 0 : 1);
		int startPage = (int) (currentPage / 10) * 10 + 1;
		int pageBlock = 10;
		int endPage = startPage + pageBlock - 1;
		if (endPage > pageCount) {
			endPage = pageCount;
		}
		request.setAttribute("memId", id);
		request.setAttribute("count", count);
		request.setAttribute("articleList", articleList);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("startPage", startPage);
		request.setAttribute("endPage", endPage);
		request.setAttribute("pageCount", pageCount);
		request.setAttribute("number", number);

		return "/faqboard/myList";
	}

	// 마이페이지에서 볼 내가 쓴 글
	@RequestMapping("myList_sub.mw") // 내가 쓴글 보기
	public String myList_sub(HttpSession session, HttpServletRequest respons, HttpServletRequest request, Model model) {

		int pageSize = 10;

		String pageNum = request.getParameter("pageNum");
		String id = (String) session.getAttribute("memId");

		if (pageNum == null) {
			pageNum = "1";
		}
		int currentPage = Integer.parseInt(pageNum);
		int start = (currentPage - 1) * pageSize + 1;
		int end = currentPage * pageSize;

		int number = 0;

		List articleList = null;

		int count = dao.getCountmy(id);
		if (count > 0) {
			articleList = dao.getArticles(start, end, id);
		}
		number = count - (currentPage - 1) * pageSize;

		int pageCount = count / pageSize + (count % pageSize == 0 ? 0 : 1);
		int startPage = (int) (currentPage / 10) * 10 + 1;
		int pageBlock = 10;
		int endPage = startPage + pageBlock - 1;
		if (endPage > pageCount) {
			endPage = pageCount;
		}
		request.setAttribute("memId", id);
		request.setAttribute("count", count);
		request.setAttribute("articleList", articleList);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("startPage", startPage);
		request.setAttribute("endPage", endPage);
		request.setAttribute("pageCount", pageCount);
		request.setAttribute("number", number);

		return "/faqboard/myList_sub";
	}
	
	@RequestMapping("content.mw") // 유저게시글 상세 보기
	public String faqContent(FaqBoardDTO dto, HttpServletRequest request, Model model, HttpSession session) {
		String pageNum = request.getParameter("pageNum");
		String number = request.getParameter("number");

		String num = request.getParameter("faq_num");
		int num1 = Integer.parseInt(num);
		
		FaqBoardDTO article = dao.getContent(num1);
		List<FaqReplyDTO> replyList = reply.faqContentReplyList(num1); //댓글출력

		model.addAttribute("number", number);
		model.addAttribute("pageNum", pageNum);
		model.addAttribute("faq_num", num);

		model.addAttribute("article", article);
		model.addAttribute("reply", replyList);

		return "/faqboard/content";
	}
	
	//유저게시글 상세보기 댓글 입출력 - ajax
	@RequestMapping("contentReply.mw")
	public String faqContentReply(int faq_num, String content, HttpSession session, Model model) {
		
		String id = (String) session.getAttribute("memId");
		
		reply.faqContentReplyInsert(id, faq_num, content); // 댓글 입력
		List<FaqReplyDTO> replyList = reply.faqContentReplyList(faq_num);// 출력
		
		model.addAttribute("faq_num", faq_num);
		model.addAttribute("reply", replyList);
		return "/faqboard/faqContentReply";
		
	}
	
	//유저게시글 상세보기 댓글 삭제 - ajax
	@RequestMapping("contentReplyDelete.mw")
	public String faqContentReplyDelete(int num, int faq_num, HttpSession session, Model model) {
		
		String id = (String)session.getAttribute("memId");
		reply.faqContentReplyDelete(id, num); // 댓글 삭제
		List<FaqReplyDTO> replyList = reply.faqContentReplyList(faq_num);// 출력

		model.addAttribute("reply", replyList);
		return "/faqboard/faqContentReply";
		
	}
	
	

	@RequestMapping("faqQwriteForm.mw") // 운영자 게시글 작성
	public String faqQwriteForm(FaqMainBoardDTO dto, HttpServletRequest request, Model model, HttpSession session) {

		int qnum = 0, qreadcount = 1;
		model.addAttribute("qnum", qnum);
		model.addAttribute("qreadcount", qreadcount);

		return "/faqboard/faqQwriteForm";
	}

	@RequestMapping("faqQwritePro.mw") // 운영자 게시글작성 실행
	public String faqQwritePro(FaqMainBoardDTO dto) {
		dao.insertQwrite(dto);

		return "/faqboard/faqQwritePro";
	}

	@RequestMapping("faqContent.mw") // 운영자 게시글 상세보기
	public String faqMainContent(FaqMainBoardDTO dto, HttpServletRequest request, Model model, HttpSession session) {
		session.getAttribute("memId");

		int qnum = Integer.parseInt(request.getParameter("qnum"));

		FaqMainBoardDTO dto1 = dao.getQcontent(qnum);

		model.addAttribute("qlist", dto1);

		return "/faqboard/faqContent";
	}

	@RequestMapping("faqMainUpdateForm.mw") // 운영자 게시글 수정
	public String faqMainUpdateForm(FaqMainBoardDTO dto1, HttpServletRequest request, Model model,
			HttpSession session) {

		String id = (String) session.getAttribute("memId");
		String q_id = (String) request.getParameter("q_id");
		int qnum = (Integer.parseInt(request.getParameter("qnum")));
		//dto로 리턴 받음
		FaqMainBoardDTO list = dao.getQcontent(qnum);

		model.addAttribute("memId", id);
		model.addAttribute("q_id", q_id);
		model.addAttribute("list", list); //dto의 객체 넘겨주기

		return "/faqboard/faqMainUpdateForm";
	}

	@RequestMapping("faqMainUpdatePro.mw") // 운영자 게시글 수정 실행
	public String faqMainUpdatePro(FaqMainBoardDTO dto, HttpServletRequest request, Model model, HttpSession session) {

		dao.updateQcontnet(dto);

		return "/faqboard/faqMainUpdatePro";
	}

	@RequestMapping("faqMainDelete.mw") // 운영자 게시글 삭제
	public String faqMainDelete(FaqMainBoardDTO dto, HttpServletRequest request, Model model, HttpSession session) {
		int qnum = (Integer.parseInt(request.getParameter("qnum")));
		String q_id = (String) session.getAttribute("memId");

		model.addAttribute("qnum", qnum);
		model.addAttribute("q_id", q_id);

		return "/faqboard/faqMainDelete";
	}

	@RequestMapping("faqMainDeletePro.mw") //운영자 게시글 삭제 진행
	public String faqMainDeletePro(FaqMainBoardDTO dto, HttpServletRequest request, Model model, HttpSession session) {

		int qnum = (Integer.parseInt(request.getParameter("qnum")));
		String q_id = (String) session.getAttribute("memId");
		//체크로 리턴 받음
		int check = dao.DeleteQcheck(qnum, q_id);
		model.addAttribute("check", check);

		if (check == 1) {
			dao.DeleteQcontent(qnum);
		}
		return "/faqboard/faqMainDeletePro";
	}
}