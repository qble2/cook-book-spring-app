package qble2.cookbook.ingredient.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UnitOfMeasureEnum {

  /** MASS AND WEIGHT **/
  @JsonProperty("gram")
  GRAM, // g

  @JsonProperty("kg")
  KILO_GRAM, // kg

  /** VOLUME **/
  @JsonProperty("mL")
  MILLI_LITER, // mL or ml

  @JsonProperty("cL")
  CENTI_LITER, // cL or cl

  @JsonProperty("dL")
  DECI_LITER, // dL or dl

  @JsonProperty("L")
  LITER, // L

  @JsonProperty("m3")
  CUBIC_METER, // m^3

  /** MIXTE **/
  @JsonProperty("teaSpoon")
  TEA_SPOON, // ~5 mL

  @JsonProperty("tableSpoon")
  TABLE_SPOON, // ~15 mL

  ;

}
