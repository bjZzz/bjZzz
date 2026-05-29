package com.nanda.asset.knowledge;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nanda.asset.domain.dto.AssetDtos.KnowledgeDocumentVO;
import com.nanda.asset.domain.dto.AssetDtos.KnowledgeImportRequest;
import com.nanda.asset.domain.entity.PubKnowledgeAuthor;
import com.nanda.asset.domain.entity.PubKnowledgeDocument;
import com.nanda.asset.domain.entity.PubKnowledgeTag;
import com.nanda.asset.mapper.PubKnowledgeAuthorMapper;
import com.nanda.asset.mapper.PubKnowledgeDocumentMapper;
import com.nanda.asset.mapper.PubKnowledgeTagMapper;
import com.nanda.asset.service.AssetOrgContext;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeDocumentService {

    private final PubKnowledgeDocumentMapper pubKnowledgeDocumentMapper;
    private final PubKnowledgeAuthorMapper pubKnowledgeAuthorMapper;
    private final PubKnowledgeTagMapper pubKnowledgeTagMapper;

    public PageResult<KnowledgeDocumentVO> search(PageQuery query, String keyword, String tag) {
        Long orgId = AssetOrgContext.requireOrgId();
        LambdaQueryWrapper<PubKnowledgeDocument> wrapper = new LambdaQueryWrapper<PubKnowledgeDocument>()
                .eq(PubKnowledgeDocument::getOrgId, orgId)
                .eq(PubKnowledgeDocument::getDeleted, 0)
                .like(keyword != null && !keyword.isEmpty(), PubKnowledgeDocument::getTitle, keyword)
                .orderByDesc(PubKnowledgeDocument::getCreatedAt);
        Page<PubKnowledgeDocument> page = pubKnowledgeDocumentMapper.selectPage(
                new Page<PubKnowledgeDocument>(query.getPage(), query.getSize()), wrapper);
        List<KnowledgeDocumentVO> items = new ArrayList<KnowledgeDocumentVO>();
        for (PubKnowledgeDocument document : page.getRecords()) {
            KnowledgeDocumentVO vo = toVO(document);
            if (tag != null && !tag.isEmpty() && (vo.getTags() == null || !vo.getTags().contains(tag))) {
                continue;
            }
            items.add(vo);
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    @Transactional
    public KnowledgeDocumentVO importDocument(KnowledgeImportRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "文档标题不能为空");
        }
        PubKnowledgeDocument document = new PubKnowledgeDocument();
        document.setId(IdGenerator.nextId());
        document.setTitle(request.getTitle());
        document.setDocType(request.getDocType() != null ? request.getDocType() : "GUIDELINE");
        document.setFileRef(request.getFileRef());
        document.setOrgId(AssetOrgContext.requireOrgId());
        document.setCreatedAt(LocalDateTime.now());
        document.setDeleted(0);
        pubKnowledgeDocumentMapper.insert(document);

        if (request.getAuthors() != null) {
            for (String author : request.getAuthors()) {
                PubKnowledgeAuthor entity = new PubKnowledgeAuthor();
                entity.setId(IdGenerator.nextId());
                entity.setDocumentId(document.getId());
                entity.setAuthorName(author);
                pubKnowledgeAuthorMapper.insert(entity);
            }
        }
        if (request.getTags() != null) {
            for (String tag : request.getTags()) {
                PubKnowledgeTag entity = new PubKnowledgeTag();
                entity.setId(IdGenerator.nextId());
                entity.setDocumentId(document.getId());
                entity.setTagName(tag);
                pubKnowledgeTagMapper.insert(entity);
            }
        }
        return toVO(document);
    }

    public KnowledgeDocumentVO getDocument(Long id) {
        PubKnowledgeDocument document = pubKnowledgeDocumentMapper.selectById(id);
        if (document == null || document.getDeleted() != null && document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "知识库文档不存在");
        }
        if (!document.getOrgId().equals(AssetOrgContext.requireOrgId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该文档");
        }
        return toVO(document);
    }

    private KnowledgeDocumentVO toVO(PubKnowledgeDocument document) {
        KnowledgeDocumentVO vo = new KnowledgeDocumentVO();
        vo.setId(document.getId());
        vo.setTitle(document.getTitle());
        vo.setDocType(document.getDocType());
        vo.setFileRef(document.getFileRef());
        vo.setCreatedAt(document.getCreatedAt());
        List<PubKnowledgeAuthor> authors = pubKnowledgeAuthorMapper.selectList(new LambdaQueryWrapper<PubKnowledgeAuthor>()
                .eq(PubKnowledgeAuthor::getDocumentId, document.getId()));
        List<String> authorNames = new ArrayList<String>();
        for (PubKnowledgeAuthor author : authors) {
            authorNames.add(author.getAuthorName());
        }
        vo.setAuthors(authorNames);
        List<PubKnowledgeTag> tags = pubKnowledgeTagMapper.selectList(new LambdaQueryWrapper<PubKnowledgeTag>()
                .eq(PubKnowledgeTag::getDocumentId, document.getId()));
        List<String> tagNames = new ArrayList<String>();
        for (PubKnowledgeTag tag : tags) {
            tagNames.add(tag.getTagName());
        }
        vo.setTags(tagNames);
        return vo;
    }
}
