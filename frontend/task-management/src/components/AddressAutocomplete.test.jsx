import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AddressAutocomplete from './AddressAutocomplete';
import * as geocodingService from '../services/geocodingService';

vi.mock('../services/geocodingService', () => ({
  searchAddresses: vi.fn(),
}));

describe('AddressAutocomplete', () => {
  const mockOnChange = vi.fn();
  const mockOnSelect = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    geocodingService.searchAddresses.mockResolvedValue([]);
  });

  it('should render input field', () => {
    render(
      <AddressAutocomplete
        value=""
        onChange={mockOnChange}
        onSelect={mockOnSelect}
        className="form-input"
        hasError={false}
      />
    );

    expect(screen.getByPlaceholderText('Start typing an address...')).toBeInTheDocument();
  });

  it('should call onChange when user types', async () => {
    const user = userEvent.setup();
    render(
      <AddressAutocomplete
        value=""
        onChange={mockOnChange}
        onSelect={mockOnSelect}
        className="form-input"
        hasError={false}
      />
    );

    const input = screen.getByPlaceholderText('Start typing an address...');
    await user.type(input, 'a');

    expect(mockOnChange).toHaveBeenCalled();
  });

  it('should search for addresses when input length >= 3', async () => {
    const user = userEvent.setup();
    geocodingService.searchAddresses.mockResolvedValue([
      { description: '123 Main St', placeId: '1' },
    ]);

    render(
      <AddressAutocomplete
        value="123"
        onChange={mockOnChange}
        onSelect={mockOnSelect}
        className="form-input"
        hasError={false}
      />
    );

    await waitFor(() => {
      expect(geocodingService.searchAddresses).toHaveBeenCalledWith('123');
    });
  });

  it('should display suggestions when available', async () => {
    geocodingService.searchAddresses.mockResolvedValue([
      { description: '123 Main St', placeId: '1' },
      { description: '456 Oak Ave', placeId: '2' },
    ]);

    render(
      <AddressAutocomplete
        value="123"
        onChange={mockOnChange}
        onSelect={mockOnSelect}
        className="form-input"
        hasError={false}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('123 Main St')).toBeInTheDocument();
      expect(screen.getByText('456 Oak Ave')).toBeInTheDocument();
    });
  });

  it('should call onSelect when suggestion is clicked', async () => {
    const user = userEvent.setup();
    geocodingService.searchAddresses.mockResolvedValue([
      { description: '123 Main St', placeId: '1' },
    ]);

    render(
      <AddressAutocomplete
        value="123"
        onChange={mockOnChange}
        onSelect={mockOnSelect}
        className="form-input"
        hasError={false}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('123 Main St')).toBeInTheDocument();
    });

    await user.click(screen.getByText('123 Main St'));

    expect(mockOnSelect).toHaveBeenCalledWith('123 Main St');
  });

  it('should hide suggestions when clicking outside', async () => {
    const user = userEvent.setup();
    geocodingService.searchAddresses.mockResolvedValue([
      { description: '123 Main St', placeId: '1' },
    ]);

    render(
      <div>
        <AddressAutocomplete
          value="123"
          onChange={mockOnChange}
          onSelect={mockOnSelect}
          className="form-input"
          hasError={false}
        />
        <button>Outside</button>
      </div>
    );

    await waitFor(() => {
      expect(screen.getByText('123 Main St')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: 'Outside' }));

    await waitFor(() => {
      expect(screen.queryByText('123 Main St')).not.toBeInTheDocument();
    });
  });

  it('should navigate suggestions with arrow keys', async () => {
    const user = userEvent.setup();
    geocodingService.searchAddresses.mockResolvedValue([
      { description: '123 Main St', placeId: '1' },
      { description: '456 Oak Ave', placeId: '2' },
    ]);

    render(
      <AddressAutocomplete
        value="123"
        onChange={mockOnChange}
        onSelect={mockOnSelect}
        className="form-input"
        hasError={false}
      />
    );

    const input = screen.getByPlaceholderText('Start typing an address...');

    await waitFor(() => {
      expect(screen.getByText('123 Main St')).toBeInTheDocument();
    });

    await user.type(input, '{arrowdown}');
    expect(screen.getByText('123 Main St')).toHaveClass('selected');

    await user.type(input, '{arrowdown}');
    expect(screen.getByText('456 Oak Ave')).toHaveClass('selected');

    await user.type(input, '{arrowup}');
    expect(screen.getByText('123 Main St')).toHaveClass('selected');
  });

  it('should select suggestion with Enter key', async () => {
    const user = userEvent.setup();
    geocodingService.searchAddresses.mockResolvedValue([
      { description: '123 Main St', placeId: '1' },
    ]);

    render(
      <AddressAutocomplete
        value="123"
        onChange={mockOnChange}
        onSelect={mockOnSelect}
        className="form-input"
        hasError={false}
      />
    );

    const input = screen.getByPlaceholderText('Start typing an address...');

    await waitFor(() => {
      expect(screen.getByText('123 Main St')).toBeInTheDocument();
    });

    await user.type(input, '{arrowdown}');
    await user.type(input, '{enter}');

    expect(mockOnSelect).toHaveBeenCalledWith('123 Main St');
  });

  it('should close suggestions with Escape key', async () => {
    const user = userEvent.setup();
    geocodingService.searchAddresses.mockResolvedValue([
      { description: '123 Main St', placeId: '1' },
    ]);

    render(
      <AddressAutocomplete
        value="123"
        onChange={mockOnChange}
        onSelect={mockOnSelect}
        className="form-input"
        hasError={false}
      />
    );

    const input = screen.getByPlaceholderText('Start typing an address...');

    await waitFor(() => {
      expect(screen.getByText('123 Main St')).toBeInTheDocument();
    });

    await user.type(input, '{escape}');

    await waitFor(() => {
      expect(screen.queryByText('123 Main St')).not.toBeInTheDocument();
    });
  });

  it('should show loading indicator while fetching', async () => {
    geocodingService.searchAddresses.mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve([]), 1000))
    );

    render(
      <AddressAutocomplete
        value="123"
        onChange={mockOnChange}
        onSelect={mockOnSelect}
        className="form-input"
        hasError={false}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('Loading...')).toBeInTheDocument();
    });
  });

  it('should not search when input length < 3', () => {
    render(
      <AddressAutocomplete
        value="12"
        onChange={mockOnChange}
        onSelect={mockOnSelect}
        className="form-input"
        hasError={false}
      />
    );

    expect(geocodingService.searchAddresses).not.toHaveBeenCalled();
  });

  it('should handle errors in address search', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});
    geocodingService.searchAddresses.mockRejectedValue(new Error('Network error'));

    render(
      <AddressAutocomplete
        value="123"
        onChange={mockOnChange}
        onSelect={mockOnSelect}
        className="form-input"
        hasError={false}
      />
    );

    await waitFor(() => {
      expect(consoleError).toHaveBeenCalledWith(
        'Error fetching address suggestions:',
        expect.any(Error)
      );
    });

    consoleError.mockRestore();
  });

  it('should apply error class when hasError is true', () => {
    render(
      <AddressAutocomplete
        value=""
        onChange={mockOnChange}
        onSelect={mockOnSelect}
        className="form-input input-error"
        hasError={true}
        ariaDescribedBy="error-message"
      />
    );

    const input = screen.getByPlaceholderText('Start typing an address...');
    expect(input).toHaveClass('input-error');
    expect(input).toHaveAttribute('aria-invalid', 'true');
    expect(input).toHaveAttribute('aria-describedby', 'error-message');
  });
});
