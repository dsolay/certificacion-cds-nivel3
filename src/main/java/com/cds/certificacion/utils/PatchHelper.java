package com.cds.certificacion.utils;

import java.util.Set;

import javax.json.JsonMergePatch;
import javax.json.JsonPatch;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.springframework.stereotype.Component;

import com.cds.certificacion.web.exception.UnprocessableEntityException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PatchHelper {

  private final ObjectMapper mapper;
  private final Validator validator;

  /**
   * @param mapper
   * @param validator
   */
  public PatchHelper(ObjectMapper mapper, Validator validator) {
    this.mapper = mapper;
    this.validator = validator;
  }

  /**
   * Performs a JSON Patch operation.
   *
   * @param patch      JSON Patch document
   * @param targetBean object that will be patched
   * @param beanClass  class of the object the will be patched
   * @param <T>
   * @return patched object
   */
  public <T> T patch(JsonPatch patch, T targetBean, Class<T> beanClass) {
      JsonStructure target = mapper.convertValue(targetBean, JsonStructure.class);
      JsonValue patched = applyPatch(patch, target);
      return convertAndValidate(patched, beanClass);
  }

  /**
   * Performs a JSON Merge Patch operation
   *
   * @param mergePatch JSON Merge Patch document
   * @param targetBean object that will be patched
   * @param beanClass  class of the object the will be patched
   * @param <T>
   * @return patched object
   */
  public <T> T mergePatch(JsonMergePatch mergePatch, T targetBean, Class<T> beanClass) {
      JsonValue target = mapper.convertValue(targetBean, JsonValue.class);
      JsonValue patched = applyMergePatch(mergePatch, target);
      return convertAndValidate(patched, beanClass);
  }

  private JsonValue applyPatch(JsonPatch patch, JsonStructure target) {
      try {
          return patch.apply(target);
      } catch (Exception e) {
          throw new UnprocessableEntityException(e);
      }
  }

  private JsonValue applyMergePatch(JsonMergePatch mergePatch, JsonValue target) {
      try {
          return mergePatch.apply(target);
      } catch (Exception e) {
          throw new UnprocessableEntityException(e);
      }
  }

  private <T> T convertAndValidate(JsonValue jsonValue, Class<T> beanClass) {
      T bean = mapper.convertValue(jsonValue, beanClass);
      validate(bean);
      return bean;
  }

  private <T> void validate(T bean) {
      Set<ConstraintViolation<T>> violations = validator.validate(bean);
      if (!violations.isEmpty()) {
          throw new ConstraintViolationException(violations);
      }
  }

}
