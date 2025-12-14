package com.kindora.kindora_backend.service;

import com.kindora.kindora_backend.dto.MemberDto;
import com.kindora.kindora_backend.model.Member;
import com.kindora.kindora_backend.model.User;

public interface MemberService {
    Member saveOrUpdate(MemberDto dto, User user);
    Member getByUser(User user);
}
