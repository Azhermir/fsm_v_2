import { useState, useEffect, useRef } from 'react';
import { searchAddresses } from '../services/geocodingService';
import './AddressAutocomplete.css';

const AddressAutocomplete = ({ value, onChange, onSelect, className, hasError, ariaDescribedBy }) => {
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const wrapperRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  useEffect(() => {
    const fetchSuggestions = async () => {
      if (value.length >= 3) {
        setIsLoading(true);
        try {
          const results = await searchAddresses(value);
          setSuggestions(results);
          setShowSuggestions(true);
        } catch (error) {
          console.error('Error fetching address suggestions:', error);
          setSuggestions([]);
        } finally {
          setIsLoading(false);
        }
      } else {
        setSuggestions([]);
        setShowSuggestions(false);
      }
    };

    const debounceTimer = setTimeout(fetchSuggestions, 300);
    return () => clearTimeout(debounceTimer);
  }, [value]);

  const handleInputChange = (e) => {
    onChange(e);
    setSelectedIndex(-1);
  };

  const handleSuggestionClick = (suggestion) => {
    onSelect(suggestion.description);
    setShowSuggestions(false);
    setSuggestions([]);
    setSelectedIndex(-1);
  };

  const handleKeyDown = (e) => {
    if (!showSuggestions || suggestions.length === 0) return;

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        setSelectedIndex((prev) =>
          prev < suggestions.length - 1 ? prev + 1 : prev
        );
        break;
      case 'ArrowUp':
        e.preventDefault();
        setSelectedIndex((prev) => (prev > 0 ? prev - 1 : -1));
        break;
      case 'Enter':
        e.preventDefault();
        if (selectedIndex >= 0) {
          handleSuggestionClick(suggestions[selectedIndex]);
        }
        break;
      case 'Escape':
        setShowSuggestions(false);
        setSelectedIndex(-1);
        break;
      default:
        break;
    }
  };

  return (
    <div className="address-autocomplete-wrapper" ref={wrapperRef}>
      <input
        type="text"
        id="clientAddress"
        name="clientAddress"
        value={value}
        onChange={handleInputChange}
        onKeyDown={handleKeyDown}
        className={className}
        placeholder="Start typing an address..."
        autoComplete="off"
        aria-invalid={hasError}
        aria-describedby={ariaDescribedBy}
        aria-autocomplete="list"
        aria-controls="address-suggestions"
        aria-expanded={showSuggestions && suggestions.length > 0}
      />
      {isLoading && <div className="autocomplete-loading">Loading...</div>}
      {showSuggestions && suggestions.length > 0 && (
        <ul className="address-suggestions" id="address-suggestions" role="listbox">
          {suggestions.map((suggestion, index) => (
            <li
              key={suggestion.placeId}
              onClick={() => handleSuggestionClick(suggestion)}
              className={`suggestion-item ${
                index === selectedIndex ? 'selected' : ''
              }`}
              role="option"
              aria-selected={index === selectedIndex}
            >
              {suggestion.description}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default AddressAutocomplete;
