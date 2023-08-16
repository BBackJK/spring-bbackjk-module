package test.bbackjk.http.sample.service;

import test.bbackjk.http.sample.dto.MemberDto;

import java.util.List;

public interface RestService {

    String basic();
    String path();
    String query1();
    String query2();
    String query3();
    String query4();
    String query5();



    String post1();
    String post2();
    MemberDto post3();
    MemberDto post31();

    MemberDto post4();
    List<MemberDto> post5();
}
