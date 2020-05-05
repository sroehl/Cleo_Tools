package com.cleo.services.jsonToVersaLexRestAPI.POJO;

import com.google.gson.internal.LinkedTreeMap;

import java.util.List;

public class VersalexCollectionResponse {

  private long totalResults;

  private long startIndex;

  private Long count;

  private List<LinkedTreeMap> resources;

  public VersalexCollectionResponse(){}

  public long getTotalResults() {
    return totalResults;
  }

  public long getStartIndex() {
    return startIndex;
  }

  public Long getCount() {
    return count;
  }

  public List<LinkedTreeMap> getResources() {
    return resources;
  }
}
